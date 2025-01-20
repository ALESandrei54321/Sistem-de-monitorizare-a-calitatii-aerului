import dht
import machine
import time
import network
import simple as mqtt
import _thread
import random

# Wi-Fi Details
ssid = 'Wokwi-GUEST'
password = ''

# MQTT Details
mqtt_server = '192.168.0.108'  # Replace with your broker's IP
client_id = 'pico_1'
topic_temp = 'senzori/temperatura'
topic_hum = 'senzori/umiditate'
topic_ppm = 'senzori/ppm'
topic_status = 'actuatori/status'  # Topic to control sensors
update_speed_topic = 'actuatori/viteza'  # Topic to control update speed

# Sensor GPIO Pins
DHT_PIN = 22
MQ_PIN = 32

# Sensor Initialization
sensor = dht.DHT22(machine.Pin(DHT_PIN))
adc = machine.ADC(machine.Pin(MQ_PIN))

# Sensor State (Default: On)
sensors_active = True

# Message rate
speed = 5

# Wi-Fi Connection
print("Connecting to Wi-Fi...")
wlan = network.WLAN(network.STA_IF)
wlan.active(True)
wlan.connect(ssid, password)

while not wlan.isconnected():
    print("Connecting...")
    time.sleep(1)

print("Connected to Wi-Fi:", wlan.ifconfig())

# Initialize MQTT Client
client = mqtt.MQTTClient(client_id, mqtt_server, port=1883)

# Sensor Reading Functions
def read_dht_sensor():
    sensor.measure()
    return sensor.temperature(), sensor.humidity()

def read_mq_sensor():
    raw_value = adc.read_u16()
    return (raw_value / 65535) * 1000

# MQTT Callback Function
def message_callback(topic, msg):
    global sensors_active
    if topic.decode() == topic_status:
        if msg.decode().lower() == "on":
            print("Sensors turned ON")
            sensors_active = True
        elif msg.decode().lower() == "off":
            print("Sensors turned OFF")
            sensors_active = False
        elif msg.decode().lower() == "update":
            print("Sensors are ON" if sensors_active else "Sensors are OFF")
    if topic.decode() == update_speed_topic:
        global speed
        speed = int(msg.decode())
        print(f"Update speed set to {speed} seconds")


# Thread Function for Subscribing
def mqtt_subscribe_thread():
    print("Starting MQTT subscribe thread...")
    client.set_callback(message_callback)
    client.connect()
    client.subscribe(topic_status)
    client.subscribe(update_speed_topic)
    print(f"Subscribed to {topic_status}")

    while True:
        client.wait_msg()  # Wait for incoming messages

# Main Function
def main():
    global sensors_active
    print("Starting main module...")

    # Start MQTT subscription thread
    _thread.start_new_thread(mqtt_subscribe_thread, ())

    # Main loop for publishing sensor data
    while True:
        if sensors_active:
            temp, hum = read_dht_sensor()
            ppm = read_mq_sensor()

            # Add some noise to the simulated sensors
            temp += random.uniform(-0.5, 0.5)
            hum += random.uniform(-0.5, 0.5)
            ppm += random.uniform(-0.5, 0.5)

            temp = round(temp, 2)
            hum = round(hum, 2)
            ppm = round(ppm, 2)


            client.publish(topic_temp, str(temp))
            client.publish(topic_hum, str(hum))
            client.publish(topic_ppm, str(ppm))

            print(f"Published -> Temp: {temp}C, Hum: {hum}%, PPM: {ppm}")
        else:
            print("Sensors are OFF. No data published.")
            # Enter deep sleep mode until sensors are turned back on

            

        time.sleep(speed)

# Run Main Function
if __name__ == "__main__":
    main()
