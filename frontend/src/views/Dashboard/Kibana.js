import React, { Component } from 'react';
import Iframe from 'react-iframe'


class Kibana extends Component {

	render () {

		return (
			<div>
				<Iframe url="http://kibana.ai-a-thon.us-south.containers.appdomain.cloud/app/kibana#/home?_g=()"
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

export default Kibana