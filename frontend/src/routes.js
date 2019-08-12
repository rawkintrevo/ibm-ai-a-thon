import React from 'react';

const Dashboard = React.lazy(() => import('./views/Dashboard'));
const AddEndpoint = React.lazy(() => import('./views/AddEndpoint'));


const routes = [
  { path: '/', exact: true, name: 'Home' },
  { path: '/dashboard', name: 'Dashboard', component: Dashboard },
  { path: '/add/endpoint', name: 'AddEndpoint', component: Dashboard },
];

export default routes;
