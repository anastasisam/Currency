'use strict';

const React = require('react');
const ReactDOM = require('react-dom');
const client = require('./client');

import DatePicker from 'react-datepicker';
import moment from 'moment';
import 'react-datepicker/dist/react-datepicker.css';
import 'bootstrap/dist/css/bootstrap.min.css';

const root = '/api';

class App extends React.Component {

	constructor(props) {
		super(props);
        let date = Date.parse(moment());
        date = new Date(date);
        date = new Date(date.setDate(date.getDate() - 1));
		this.state = {purchaseDate: date, maxDate: date, purchaseSum: 1, gain: ''};
		this.onPurchase = this.onPurchase.bind(this);
		this.handleChangeDate = this.handleChangeDate.bind(this);
		this.handleChangeSum = this.handleChangeSum.bind(this);
	}

	onPurchase() {
        if (!this.state.purchaseSum) {
            this.setState({
                purchaseDate: this.state.purchaseDate,
                maxDate: this.state.maxDate,
                purchaseSum: "1",
                gain: this.state.gain
            })
        }
		client({method: 'GET', path: root + '/purchase', params: {
			purchaseDate: this.state.purchaseDate.getTime(),
			purchaseSum: this.state.purchaseSum}})
			.then(response => {console.log("testlog=" + response); return response;}).done(response => {
            this.setState({
                purchaseDate: this.state.purchaseDate,
                maxDate: this.state.maxDate,
                purchaseSum: this.state.purchaseSum,
                gain: response.entity.gain
            });
		});
	}

    handleChangeDate(date) {
        this.setState({
            purchaseDate: date,
            maxDate: this.state.maxDate,
            purchaseSum: this.state.purchaseSum,
            gain: this.state.gain
        })
    }

    handleChangeSum(event) {
		if (event.target.validity.valid) {
            this.setState({
                purchaseDate: this.state.purchaseDate,
                maxDate: this.state.maxDate,
                purchaseSum: event.target.value,
                gain: this.state.gain
            })
        }
    }

	componentDidMount() {
	}

	render() {
		return (
			<div className="main-div">
				<div className="title-class">Currency gain</div>
				<table className="table-class">
					<tbody>
						<tr>
							<td><div className="label-class">Date</div></td>
							<td><DatePicker
								name="startDate"
								dateFormat="yyyy-MM-dd"
								maxDate={this.state.maxDate}
								selected={this.state.purchaseDate}
								onChange={this.handleChangeDate} /></td>
						</tr>
						<tr>
							<td><div className="label-class">Amount USD</div></td>
							<td><input type="text" pattern="[1-9]{1}[0-9]*" onChange={this.handleChangeSum}
									   value={this.state.purchaseSum}/></td>
						</tr>
						<tr>
							<td><div className="label-class">Gain</div></td>
							<td><div className="gain-class">{this.state.gain}</div></td>
						</tr>
					</tbody>
				</table>
				<button onClick={this.onPurchase}>Recalculate</button>
			</div>
		)
	}
}

ReactDOM.render(
	<App />,
	document.getElementById('react')
);
