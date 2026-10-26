#Changelog

Todos los cambios importantes en este proyecto serán documentados en este archivo.

Este proyecto sigue versionado semántico (SemVer) y el formato de [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/).

## [1.0.0] - 2026-10-26

### Added

- Proyecto Maven `cfdi-reader`para implementar la logica de negocio de carga de información de los archivos XML del CFDI 4.0 del SAT, compatible con JDK 8.
- Tipos de CFDI soportados:
  - CFDI 4.0 Tipo Ingresos.
  - Complemento de pago 2.0
  - Complemento de Nómina 1.2
- Soporte para lectura del XML del SAT, desde InputStream y Filesystem.
- Depende de [cfdi40-model](https://github.com/eabadillom/cfdi40-model) versión 1.0.0 