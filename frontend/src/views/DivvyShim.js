import React, { Component} from 'react';
import { Form, TextInput, NumberInput, Button } from 'carbon-components-react';

class DivvyShim extends Component {

	constructor(props) {
		super(props)

		this.state = {
			stations: "49,50,51",
			nRecords: 20,
			orgId: "dwbsnh",
			typeId: "divvyBike",
			sendingNewData: false
		}

		this.onChange = this.onChange.bind(this);
		this.handleSubmit = this.handleSubmit.bind(this);
	}

	onChange(e) {
		console.log(e.target.id, e.target.value);
		this.setState({ [e.target.id]: e.value})
	}

	handleSubmit(event) {
//    this.setState({sendingNewData: true})

		console.log('this submit...')
    // basic record
    var data = {
      stations: this.state.stations,
      nRecords: this.state.nRecords,
      orgId: "dwbsnh",
      typeId: "divvyBike",

    }


    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = () => {
         if (xhr.readyState === 4) {
          this.setState({sendingNewData: false})
					console.log("is done!")
         }
        }


    var url = "https://a5056918.us-south.apiconnect.appdomain.cloud/aiathon/data"

    xhr.open("POST", url, true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send(JSON.stringify(data));

  }

	render () {
		return(
		<div>
			<div>
						 <h2
	                      style={{
	                        fontWeight: "800",
	                        margin: "30px 0",
	                        fontSize: "20px"
	                      }}
	                    >WatsonIoT Divvy Shim</h2>
						WatsonIoT Platform-Lite has data throughput restrictions. As such we have created a limited fire shim to prevent
						the account from being turned off from excessive use (it happened a few times in dev).


			</div>
			<div>

			</div>
			<div>
				<Form>
	        <TextInput id="stations" labelText="Stations" value={this.state.stations} onChange={this.onChange}/>
	        <NumberInput id='nRecords'
	            label='Records Per Station'
	            min={1}
              max={100}
              value={this.state.nRecords}
              onChange={this.onChange}
               />
					<Button onClick={this.handleSubmit}>Pump<i className="bx-apim--loading"/></Button>
	      </Form>

	    </div>
	  </div>
		)
	}
}

export default DivvyShim;