import React, { Component } from 'react';

import { Table, TableRow, TableHead, TableHeader, TableCell, TableBody } from 'carbon-components-react';

const headers = [
  {
    // `key` is the name of the field on the row object itself for the header
    key: 'id',
    // `header` will be the name you want rendered in the Table Header
    header: 'ID',
  },
  {
    key: 'name',
    header: 'Name',
  },
  {
    key: 'url',
    header: 'URL',
  },
];

class ViewEndpoints extends Component {

	constructor(props) {
		super(props)

		this.state = {
			tableData: []
		}
	}

	componentDidMount() {
		var url = "https://a5056918.us-south.apiconnect.appdomain.cloud/aiathon/endpointserver"
    		fetch(url)
        					.then(data => {return data.json()})
        					.then(function(res) {
        					  this.setState({ tableData: res.map( i => {

                               						<TableRow>

                               							<TableCell>baz</TableCell>
                               							<TableCell>fo</TableCell>
                               						</TableRow>
                               						})})})



	}

	render() {
		console.log("this.state.endpointData", this.state.endpointData)
		return (
			<div>

			<Table>

				<TableHead>
					<TableRow>

            <TableHeader >
              Name
            </TableHeader>
            <TableHeader >
              Url
            </TableHeader>
					</TableRow>
				</TableHead>
				<TableBody>
					{this.state.tableData}
				</TableBody>

			</Table>
			</div>
		)
	}
}

export default ViewEndpoints;