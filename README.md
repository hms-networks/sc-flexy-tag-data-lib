# sc-flexy-tag-data-lib

## DEPRECATION NOTICE
This repository (and library) have been deprecated and will no longer receive updates or maintenance. 
This functionality has been relocated to the [hms-networks/sc-ewon-flexy-extensions-lib](https://github.com/hms-networks/sc-ewon-flexy-extensions-lib) repository.

THE CODE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND. HMS DOES NOT WARRANT THAT THE FUNCTIONS OF THE CODE WILL MEET YOUR REQUIREMENTS, OR THAT THE OPERATION OF THE CODE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT DEFECTS IN IT CAN BE CORRECTED.
---

## [Table of Contents](#table-of-contents)

1. [Description](#description)
   1. [Memory Heap Size Notice](#memory-heap-size-notice)
2. [Developer Documentation](#developer-documentation)
3. [Dependencies](#dependencies)

---

## [Description](#table-of-contents)

A library to get historical or real time tag values.

### [Memory Heap Size Notice](#table-of-contents)

The operations performed in the historical data portions of this library use a significant amount of memory, and it is recommended that the Ewon Flexy Java heap size be increased to 25M (25 MB) or greater.
Failure to do so may result in slow performance or unexpected behavior when using the historical data functionality of this library.

## [Developer Documentation](#table-of-contents)

Developer documentation is available in Javadoc format found in the release package [https://github.com/hms-networks/sc-flexy-tag-data-lib/releases](https://github.com/hms-networks/sc-flexy-tag-data-lib/releases).

## [Dependencies](#table-of-contents)
1. [sc-flexy-string-lib](https://github.com/hms-networks/sc-flexy-string-lib)
2. [sc-flexy-datapoint-lib](https://github.com/hms-networks/sc-flexy-datapoint-lib)
3. [sc-flexy-tag-info-lib](https://github.com/hms-networks/sc-flexy-tag-info-lib)
4. [sc-flexy-file-util-lib](https://github.com/hms-networks/sc-flexy-file-util-lib)
5. [sc-flexy-logger-lib](https://github.com/hms-networks/sc-flexy-logger-lib)
