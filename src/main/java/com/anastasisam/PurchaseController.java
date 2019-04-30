package com.anastasisam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

/**
 * @author ASamodurova
 */
@RestController
@RequestMapping(value = PurchaseController.PATH_ROOT)
public class PurchaseController {

    @Value("${fixed.spread}")
    private String fixedSpread;

    @Autowired
    protected UrlsConnectedWithCurrensyCourse urls;

    static final String PATH_ROOT = "/api";
    private static final String PATH_PURCHASE = "/purchase";

    @GetMapping(value = PurchaseController.PATH_PURCHASE, produces= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> recalculate(@RequestParam("purchaseDate") Long purchaseDate,
                                                       @RequestParam("purchaseSum") String purchaseSum) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            // https://openexchangerates.org/api/historical/2019-02-01.json?app_id=6aeba3cd84f941fdbe38676755b32e7d&base=USD&symbols=RUB
            ResponseEntity<String> responseHistorical = restTemplate.getForEntity(urls.getHistoricalRequestUrl(purchaseDate), String.class);
            ObjectMapper mapper = new ObjectMapper();
            /*
            {
              "disclaimer": "Usage subject to terms: https://openexchangerates.org/terms",
              "license": "https://openexchangerates.org/license",
              "timestamp": 1549065540,
              "base": "USD",
              "rates": {
                "RUB": 65.455
              }
            }
             */
            BigDecimal spread = new BigDecimal(fixedSpread);
            BigDecimal purchaseSumBigDecimal = new BigDecimal(purchaseSum);

            BigDecimal ratesInRubHistorical = new BigDecimal(getRatesInRub(mapper, responseHistorical)).add(spread);
            BigDecimal purchaseInRub = ratesInRubHistorical.multiply(purchaseSumBigDecimal);

            // https://openexchangerates.org/api/latest.json?app_id=6aeba3cd84f941fdbe38676755b32e7d&base=USD&symbols=RUB
            ResponseEntity<String> responseLatest = restTemplate.getForEntity(urls.getLatestRequestUrl(), String.class);
            getRatesInRub(mapper, responseLatest);
            /*
            {
              "disclaimer": "Usage subject to terms: https://openexchangerates.org/terms",
              "license": "https://openexchangerates.org/license",
              "timestamp": 1556488791,
              "base": "USD",
              "rates": {
                "RUB": 64.776408
              }
            }
             */
            BigDecimal ratesInRubLatest = new BigDecimal(getRatesInRub(mapper, responseLatest)).subtract(spread);
            BigDecimal sellInRub = ratesInRubLatest.multiply(purchaseSumBigDecimal);

            BigDecimal gain = sellInRub.subtract(purchaseInRub);

            BigDecimal displayVal = gain.setScale(2, RoundingMode.HALF_EVEN);
            NumberFormat rubCostFormat = NumberFormat.getCurrencyInstance();
            rubCostFormat.setMinimumFractionDigits(1);
            rubCostFormat.setMaximumFractionDigits(2);

            return ResponseEntity.ok(String.format("{\"gain\": \"%s\"}", rubCostFormat.format(displayVal.doubleValue())));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String getRatesInRub(ObjectMapper mapper, ResponseEntity<String> responseLatest) throws IOException {
        JsonNode responseJsonNode;
        String ratesInRub = null;
        if (responseLatest.getStatusCode() != HttpStatus.OK || responseLatest.getBody() == null ||
                (responseJsonNode = mapper.readTree(responseLatest.getBody())) == null ||
                responseJsonNode.path("rates") == null ||
                responseJsonNode.path("rates").path("RUB") == null ||
                (ratesInRub = responseJsonNode.path("rates").path("RUB").asText()) == null ||
                "".equals(ratesInRub)) {
            throw new RuntimeException(String.format("Currency server error or unexpected response format: status code = %d, body = %s",
                    responseLatest.getStatusCode().value(), responseLatest.getBody()));
        }
        return ratesInRub;
    }
}
