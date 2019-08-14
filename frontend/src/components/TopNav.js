import React, { Component} from 'react';
import {
  Header,
  HeaderName,
  SkipToContent,

} from "carbon-components-react/lib/components/UIShell";

import MainSideNav from "./MainSideNav"

class TopNav extends Component{

	render() {
		return(
			<div>
			<Header aria-label="Rawkintreov's House of Realtime IoT Analtics">
			<SkipToContent />
        <HeaderName href="#" prefix="Rawkintrevo's House of Realtime IoT Analytics">
          [Platform]
        </HeaderName>
      </Header>
      <MainSideNav/>
      </div>

		)
	}
}

export default TopNav

