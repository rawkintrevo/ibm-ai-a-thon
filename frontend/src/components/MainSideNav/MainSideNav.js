
import React, { Component } from 'react';

import {
  Content,
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
	        <SideNavMenu title="Dashboard">
	          <SideNavMenuItem href="javascript:void(0)">
	            Dashboard 1
	          </SideNavMenuItem>
	          <SideNavMenuItem href="javascript:void(0)"> {/* aria-current="page" add this for selected page. */}
	            Dashboard 2
	          </SideNavMenuItem>
	          <SideNavMenuItem href="javascript:void(0)">
	            Dashboard 3
	          </SideNavMenuItem>
	        </SideNavMenu>
	        <SideNavMenu title="Models">
	          <SideNavMenuItem href="javascript:void(0)">
	            See Current Model Endpoints
	          </SideNavMenuItem>
	          <SideNavMenuItem href="javascript:void(0)"> {/* aria-current="page" */}
	            Add New Model Endpoint
	          </SideNavMenuItem>
	        </SideNavMenu>
	        <SideNavLink href="javascript:void(0)">Create New Stream</SideNavLink>
	        <SideNavLink href="https://github.ibm.com/trevor-grant/ibm-ai-a-thon">Github Source Code</SideNavLink>
	      </SideNavItems>
	    </SideNav>
		)
	}
}

export default MainSideNav;