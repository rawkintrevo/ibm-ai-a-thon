import React from 'react';

const Dashboard = React.lazy(() => import('./views/Dashboard'));
const AllStations = React.lazy(() => import('./views/Dashboard/AllStations'));
const AddEndpoint = React.lazy(() => import('./views/AddEndpoint'));


const routes = [
  { path: '/', exact: true, name: 'Home' },
  { path: '/dashboard', name: 'Dashboard', component: Dashboard },
  { path: '/dashboard/allstations', name: 'AllStations', component: AllStations },
  { path: '/add/endpoint', name: 'AddEndpoint', component: Dashboard },
];

export default routes;
