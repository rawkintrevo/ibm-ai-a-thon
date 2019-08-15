import React from 'react';

const Dashboard = React.lazy(() => import('./views/Dashboard/Dashboard.js'));
const Kibana = React.lazy(() => import('./views/Dashboard/Kibana.js'));
const Flink = React.lazy(() => import('./views/Dashboard/Flink.js'));
const AddEndpoint = React.lazy(() => import('./views/AddEndpoint'));
const DivvyShim = React.lazy(() => import('./views/DivvyShim.js'));
const Endpoints = React.lazy(() => import('./views/ViewEndpoints.js'));

const routes = [
  { path: '/', exact: true, name: 'Home' },
  { path: '/dashboard/dashboard', name: 'Dashboard', component: Dashboard },
  { path: '/underhood/kibana', name: 'Kibana', component: Kibana },
  { path: '/underhood/flink', name: 'Flink', component: Flink },
  { path: '/add/endpoint', name: 'AddEndpoint', component: AddEndpoint },
  { path: '/divvyshim', name: 'DivvyShim', component: DivvyShim },
  { path: '/model/endpoints', name: 'Endpoints', component: Endpoints },
];

export default routes;
