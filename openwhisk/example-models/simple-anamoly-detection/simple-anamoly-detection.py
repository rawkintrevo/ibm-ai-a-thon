
import sys
import json

def main(params):

    analytics = json.loads(params['analytics'])
    print("------- analytics keys")
    print(analytics.keys())
    print(analytics['properties'].keys())
    avgDict = analytics['properties']['avg']
    stdDict = analytics['properties']['std']

    # print("----------------")

    device_data = json.loads(params['device_data'])


    output_json = {
        "device_data" : device_data
    }


    for metric in device_data.keys():
        ## try catch is important bc some values may be strings and that will cause this to fail
        ## another (better) approach would be to just ping the values you know and care about...
        try:
            value = float(device_data[metric])
        except:
            pass
        if value > (avgDict[metric] + stdDict[metric]):
            output_json['simple_anomaly_detection_'+metric] = "danger-high"
        elif value < (avgDict[metric] - stdDict[metric]):
            output_json['simple_anomaly_detection_'+metric] = "danger-low"
        else:
            output_json['simple_anomaly_detection_'+metric] = "all-is-well"

    return output_json