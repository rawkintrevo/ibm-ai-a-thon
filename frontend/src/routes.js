import React from 'react';

const Dashboard = React.lazy(() => import('./views/Dashboard/Dashboard.js'));
const Kibana = React.lazy(() => import('./views/Dashboard/Kibana.js'));
const AddEndpoint = React.lazy(() => import('./views/AddEndpoint'));


const routes = [
  { path: '/', exact: true, name: 'Home' },
  { path: '/dashboard/dashboard', name: 'Dashboard', component: Dashboard },
  { path: '/underhood/kibana', name: 'Kibana', component: Kibana },
  { path: '/add/endpoint', name: 'AddEndpoint', component: AddEndpoint },
];

export default routes;
