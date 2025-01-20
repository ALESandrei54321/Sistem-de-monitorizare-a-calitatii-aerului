# SPDX-FileCopyrightText: © 2022 Uri Shaked
# SPDX-License-Identifier: MIT

SOURCES = chips/mq135.chip.c
TARGET = dist/chip.wasm

.PHONY: all
all: $(TARGET) dist/chip.json

.PHONY: clean
clean:
	rm -rf dist

dist:
	mkdir -p dist

$(TARGET): dist $(SOURCES)
	emcc $(SOURCES) -o $(TARGET) -s -nostartfile WASM=1 -s SIDE_MODULE=1 -s EXPORT_ALL=1 -s ERROR_ON_UNDEFINED_SYMBOLS=0

dist/chip.json: chip.json
	cp chip.json dist
