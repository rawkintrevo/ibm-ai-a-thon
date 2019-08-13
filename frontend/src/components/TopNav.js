import React, { Component, Suspense } from 'react';
import {
  Header,
  HeaderName
} from "carbon-components-react/lib/components/UIShell";

class TopNav extends Component{

	render() {
		return(
			<Header aria-label="Rawkintreov's House of Realtime IoT Analtics">
        <HeaderName href="#" prefix="Rawkintrevo's House of Realtime IoT Analytics">
          [Platform]
        </HeaderName>
      </Header>
		)
	}
}

export default TopNav

