from requests import get

from wiotp.sdk.api.registry.devices import DeviceCreateRequest
from wiotp.sdk.application import ApplicationClient

r = get("https://gbfs.divvybikes.com/gbfs/gbfs.json")
feeds = r.json()['data']['en']['feeds']

orgId = "snpaca"
typeId = "divvyBike"

stations = get(feeds[3]['url']).json()['data']['stations']


myConfig = {
    "identity": {"appId": "Divvy Shim"},
    "auth" : { "key": "a-snpaca-ebawmcz3mh",
               "token": "*2_sGnoMfI?FJ*0r(B"
               }
}

client = ApplicationClient(config=myConfig, logHandlers=None)


devicesToRegister = []
for s in stations:
    print(s['station_id'])
    dReq = DeviceCreateRequest(typeId= typeId, deviceId= s['station_id'], authToken= s['station_id'].ljust(8, "0") )
    registrationResult = client.registry.devices.create([dReq])


## We only get 541 of them :'( snpaca maximum reached. Limit 500
