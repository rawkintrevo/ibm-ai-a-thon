
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
	          <SideNavMenuItem href="#/dashboard/dashboard"> {/* aria-current="page" add this for selected page. */}
	            Dashboard
	          </SideNavMenuItem>
	          <SideNavMenuItem href="/">
	            Dashboard 3
	          </SideNavMenuItem>
	        </SideNavMenu>
	        <SideNavMenu title="Models">
	          <SideNavMenuItem href="/">
	            See Current Model Endpoints
	          </SideNavMenuItem>
	          <SideNavMenuItem href="#/add/endpoint"> {/* aria-current="page" */}
	            Add New Model Endpoint
	          </SideNavMenuItem>
	        </SideNavMenu>
	        <SideNavLink href="/">Create New Stream</SideNavLink>
	        <SideNavLink href="https://github.ibm.com/trevor-grant/ibm-ai-a-thon">Github Source Code</SideNavLink>
	      </SideNavItems>
	    </SideNav>
		)
	}
}

export default MainSideNav;