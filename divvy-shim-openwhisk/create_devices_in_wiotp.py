from requests import get

from wiotp.sdk.api.registry.devices import DeviceCreateRequest
from wiotp.sdk.application import ApplicationClient

## Remember to add Divvy Device Type first!

r = get("https://gbfs.divvybikes.com/gbfs/gbfs.json")
feeds = r.json()['data']['en']['feeds']

orgId = "dwbsnh"
typeId = "divvyBike"
range_start = 50
range_end = 53

stations = get(feeds[3]['url']).json()['data']['stations'][range_start:range_end]


myConfig = {
    "identity": {"appId": "Divvy Shim"},
    "auth" : { "key": "a-dwbsnh-s06bdobyc6",
               "token": "i2e0OyhT*HHxx&7OOa"
               }
}

client = ApplicationClient(config=myConfig, logHandlers=None)


devicesToRegister = []
for s in stations:
    print(s['station_id'])
    dReq = DeviceCreateRequest(typeId= typeId, deviceId= s['station_id'], authToken= s['station_id'].ljust(8, "0") )
    registrationResult = client.registry.devices.create([dReq])


## We only get 541 of them :'( snpaca maximum reached. Limit 500
