# IoT System for Air Quality Monitoring

## Overview

This project focuses on creating an intelligent system to monitor various air parameters continuously, integrating multiple sensors and actuators. It provides real-time data on temperature, humidity, and air quality based on surrounding gas concentrations.

Key Features:
- **Sensors**: Utilizes DHT11 (temperature and humidity) and MQ135 (chemical compounds in the air, e.g., NH3, CO2, etc.).
- **Communication**: Data is transmitted using the MQTT protocol to a local server hosted on a laptop.
- **Visualization**: An Android app offers an interactive dashboard to display data in graphs.
- **Control**: Users can remotely start/stop data collection to optimize power consumption.

---

## Architecture

The system employs a **star-based IoT network**:
- Sensors and users connect to a central MQTT server.
- Implements **Edge Computing** for local data processing.

### Components:
1. **Sensors**:
   - **DHT11**: Measures temperature and humidity.
   - **MQ135**: Monitors chemical pollutants.
2. **Microcontroller**:
   - Simulated using **ESP32** for wireless communication.
3. **MQTT Server**:
   - Broker implemented using Mosquitto on port 1883.
4. **Android Application**:
   - Displays real-time data and allows control of the sensor state.

---

## Implementation Details

1. **Data Collection**:
   - Uses Wokwi simulator for testing with ESP32 microcontroller.
   - Simulates sensor readings with realistic noise for testing purposes.
   - Communication between sensors and the MQTT broker is implemented in `simple.py`.

2. **Data Visualization**:
   - Android app, developed in Android Studio (Java & XML), visualizes:
     - Temperature: Time-series graph.
     - Humidity and gas concentration: Speedometer-style gauges.

3. **Notifications**:
   - If gas levels exceed a threshold (600 ppm), the app sends alerts to users.

---

## Tools and Dependencies

### Required Software:
- **Wokwi Extension**: For simulated circuits.
- **Mosquitto**: For MQTT server.
- **Android Studio**: For building the Android app.

### Required Files:
- `simple.py`: Implements MQTT communication.
- `Wokwi-api.h`: Supports custom chip simulation.
- `ESP32_GENERIC-20240602-v1.23.0.bin`: ESP32 firmware for WiFi functionality.

---
