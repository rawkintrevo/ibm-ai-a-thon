
import React, { Component } from 'react';

import {

  SideNav,
  SideNavItems,
  SideNavLink,
  SideNavMenu,
  SideNavMenuItem
} from "carbon-components-react/lib/components/UIShell";


class MainSideNav extends Component {

	render () {
		return (
			<SideNav
	      isFixedNav
	      expanded={true}

	      isChildOfHeader={false}
	      aria-label="Side navigation"
	    >
	      <SideNavItems>
	        <SideNavMenu title="Under The Hood">
	          <SideNavMenuItem href="#/underhood/kibana">
	            Kibana Dashboard
	          </SideNavMenuItem>
	          <SideNavMenuItem href="#/underhood/flink"> {/* aria-current="page" add this for selected page. */}
	            Flink WebUI
	          </SideNavMenuItem>
	          <SideNavMenuItem href="#/divvyshim">
	            Pump Divvy Data
	          </SideNavMenuItem>
	        </SideNavMenu>
	        <SideNavMenu title="Models">
	          <SideNavMenuItem href="#/model/endpoints">
	            See Current Model Endpoints
	          </SideNavMenuItem>
	          <SideNavMenuItem href="#/add/endpoint"> {/* aria-current="page" */}
	            Add New Model Endpoint
	          </SideNavMenuItem>
	        </SideNavMenu>

	        <SideNavLink disabled>Create New Stream (disabled)</SideNavLink>
	        <SideNavLink href="https://github.ibm.com/trevor-grant/ibm-ai-a-thon">Github Source Code</SideNavLink>
	      </SideNavItems>
	    </SideNav>
		)
	}
}

export default MainSideNav;