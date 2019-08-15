import React, { Component } from 'react';
import Iframe from 'react-iframe'


class Flink extends Component {

	render () {

		return (
		<div>
			<div>
				<Iframe url="http://flink.ai-a-thon.us-south.containers.appdomain.cloud/#/overview"
                width="1200px"
                height="1000px"
                id="flink"
                className="myClassname"
                display="initial"
                position="relative"/>

        </div>
        <div>
          <code>--orgid dwbsnh  --appid org_rawkintrevo_aiathon --apikey a-dwbsnh-s06bdobyc6 --authtoken i2e0OyhT*HHxx&7OOa --esurl elasticsearch --endpointurl https://a5056918.us-south.apiconnect.appdomain.cloud/aiathon/endpointserver</code>
        </div>
      </div>
		)
	}
}

export default Flink