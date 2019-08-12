import React, { Component, Suspense } from 'react';
import { BrowserRouter, Redirect, Route, Switch } from 'react-router-dom';
import * as router from 'react-router-dom';
import { Container } from 'reactstrap';

import MainSideNav from "../MainSideNav"

import routes from '../../routes';

class Layout extends Component {

	loading = () => <div className="animated fadeIn pt-1 text-center">Loading...</div>

	render () {
		return (
			<div>
				<MainSideNav />
				<main className="main">
          <Container fluid>
            <Suspense fallback={this.loading()}>
              <BrowserRouter>
	              <Switch>
	                {routes.map((route, idx) => {
	                  return route.component ? (
	                    <Route
	                      key={idx}
	                      path={route.path}
	                      exact={route.exact}
	                      name={route.name}
	                      render={props => (
	                        <route.component {...props} />
	                      )} />
	                  ) : (null);
	                })}
	                <Redirect from="/" to="/dashboard" />
	              </Switch>
	            </BrowserRouter>
            </Suspense>
          </Container>
        </main>
			</div>

		)
	}
}

export default Layout;