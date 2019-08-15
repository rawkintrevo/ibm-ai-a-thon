
from requests import get
from pprint import pprint
from time import sleep


orgId = "dwbsnh"
typeId = "divvyBike"


r = get("https://gbfs.divvybikes.com/gbfs/gbfs.json")
feeds = r.json()['data']['en']['feeds']

station_status_url = [d['url'] for d in feeds if d['name']=='station_status'][0]
resp = get(station_status_url).json()['data']['stations']

stations = [r for r in resp if r['station_id'] in ['49', '50', '51']]

from wiotp.sdk.device import DeviceClient
deviceOptions = {
    s['station_id']: {
        "identity": {"orgId": orgId, "typeId": typeId, "deviceId": s['station_id']},
        "auth": {"token": s['station_id'].ljust(8, "0")},
    } for s in stations }

deviceCli = {s['station_id'] : DeviceClient(deviceOptions[s['station_id']]) for s in stations}

for s in stations:
    deviceCli[s['station_id']].connect()
# to create auth- station_id .ljust(8, "0") so 655 -> 65500000
# for s in station_data: print(s['station_id'].ljust(8, "0"))

from datetime import datetime

def eventPublishCallback():
    print("Device Publish Event done!!!")

activeIds = deviceCli.keys()

station_status_url = [d['url'] for d in feeds if d['name']=='station_status'][0]
# for i in range(0,20):
while True:
    loop_sp = datetime.now()
    station_data = get(station_status_url).json()['data']['stations']
    for d in station_data:
        if d['station_id'] not in activeIds:
            continue
        d.update({'shim date' : datetime.now().strftime("%Y/%m/%d %H:%M:%S Z")})
        deviceCli[d['station_id']].connect()
        deviceCli[d['station_id']].publishEvent(eventId="status", msgFormat="json", data=d, qos=0, onPublish=eventPublishCallback)
        deviceCli[d['station_id']].disconnect()
    loop_ep = datetime.now() # then seep for 5 - sp-ep seconds
    print((loop_ep - loop_sp).seconds)
    sleep(5)