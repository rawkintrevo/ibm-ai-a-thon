import React, { Component } from 'react';
import Iframe from 'react-iframe'


class AllStations extends Component {

	render () {
		console.log("allstations")
		return (
			<div>
				<Iframe url="http://kibana.ai-a-thon.us-south.containers.appdomain.cloud/goto/04af3856820988276648c6122f2ed251?embed=true"
                width="1200px"
                height="1000px"
                id="kibanaa"
                className="myClassname"
                display="initial"
                position="relative"/>

          </div>
		)
	}
}

export default AllStations