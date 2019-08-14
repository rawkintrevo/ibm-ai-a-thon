import React, { Component, Suspense } from 'react';
import { Redirect, Route, Switch } from 'react-router-dom';

import TopNav from "../TopNav"
import routes from '../../routes';

class Layout extends Component {


	loading = () => <div className="animated fadeIn pt-1 text-center">Loading...</div>

	render () {
		console.log("routes", routes)

		return (
		<div className="container">


          <TopNav />
          <div className="bx--grid">
            <div className="bx--row">
              <section className="bx--offset-lg-2 bx--col-lg-13">
                <h2
                  style={{
                    fontWeight: "800",
                    margin: "30px 0",
                    fontSize: "20px"
                  }}
                >
                  Purpose and function
                </h2>
                <Suspense fallback={this.loading()}>
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
                        <Redirect from="/" to="/home" />
                      </Switch>
                  </Suspense>
                </section>
                </div>
              </div>

				</div>


		)
	}
}

export default Layout;