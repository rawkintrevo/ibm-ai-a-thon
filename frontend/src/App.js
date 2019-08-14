import React, { Component } from 'react';
import { HashRouter, Route, Switch } from 'react-router-dom';
import './App.css';

import Layout from "./components/Layout"

class App extends Component {
  render() {
    return (
      <HashRouter>
        <Switch>
          <Route path="/" name="Home" render={ props => <Layout {...props}/>}/>
        </Switch>
      </HashRouter>
    );
  }
}

export default App;
