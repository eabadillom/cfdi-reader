# cfdi-reader

Librería modular y portátil escrita en **Java SE** para la lectura, validación de negocio y preparación de impresión del estándar **CFDI 4.0 (SAT México)**. 

Este proyecto está diseñado bajo los patrones de diseño **Factory** y **Strategy**, aislando el código de JAXB de las reglas de negocio particulares de cada tipo de comprobante. Almacena de forma nativa los archivos visuales de JasperReports, entregando un arreglo de bytes (`byte[]`) listo para ser consumido por APIs web, aplicaciones de escritorio o sistemas de almacenamiento.

---

## 🛠️ Arquitectura del Proyecto

El ecosistema está desacoplado en dos componentes clave:
1. **`cfdi40-model`**: Contiene exclusivamente las clases planas (POJOs) generadas por `xjc` a partir de los esquemas `.xsd` del SAT (`Comprobante`, `Pagos`, `Nomina`). independiente de la lógica visual o persistencia.
2. **`cfdi-reader`** (Este proyecto): Contiene los procesadores específicos, la carga unmarshal de JAXB, los reportes compilados `.jasper` y la exportación final a PDF.

---

## 📝 Resumen de Parámetros para JasperReports

El diseño permite que cada tipo de CFDI aporte de manera dinámica listas independientes de objetos aplanados. Esto evita expresiones de casting complejas dentro del `.jrxml`.

Al diseñar o extender formatos en el futuro, declara los siguientes parámetros en el reporte principal según sea el caso:

| Nombre del Parámetro | Tipo de Dato en Jasper (`Value Class Name`) | Ámbito | Descripción / Uso |
| :--- | :--- | :--- | :--- |
| **`DS_CONCEPTOS`** | `net.sf.jasperreports.engine.JRDataSource` | **Global** | Colección genérica de conceptos de la factura principal. |
| **`DS_EMISOR`** | `net.sf.jasperreports.engine.JRDataSource` | **Global** | Colección genérica del Emisor. |
| **`DS_RECEPTOR`** | `net.sf.jasperreports.engine.JRDataSource` | **Global** | Colección genérica del Receptor. |
| **`DS_IMPUESTOS`** | `net.sf.jasperreports.engine.JRDataSource` | **Global** | Colección genérica de Impuestos del CFDI. |
| **`DS_TFD`** | `net.sf.jasperreports.engine.JRDataSource` | **Global** | Colección genérica del Timbre Fiscal Digital. |
| **`SUBREPORT_DIR`** | `java.lang.String` | **Global** | Ruta interna del Classpath (ej. `reports/factura/`) para enlazar subreportes relacionados al CFDI v4.0. |
| **`SUBREPORT_DIR_PAGOS`** | `java.lang.String` | **Global** | Ruta interna del Classpath (ej. `reports/pagos/`) para enlazar subreportes relacionados al complemento de Pago 2.0. |
| **`DS_PAGOS`** | `net.sf.jasperreports.engine.JRDataSource` | *Complemento Pagos* | Lista de objetos `mx.gob.sat.pagos20.Pagos.Pago`. |
| **`DS_NOMINA_CORE`** | `net.sf.jasperreports.engine.JRDataSource` | *Complemento Nómina* | Nodo base encapsulado `mx.gob.sat.nomina12.Nomina`. |
| **`DS_PERCEPCIONES`**| `net.sf.jasperreports.engine.JRDataSource` | *Complemento Nómina* | Conceptos de ingresos ordinarios/extraordinarios. |
| **`DS_DEDUCCIONES`** | `net.sf.jasperreports.engine.JRDataSource` | *Complemento Nómina* | Retenciones, impuestos y descuentos del empleado. |

> 💡 **Nota de Extensibilidad:** Si en el futuro se incorpora un nuevo complemento (por ejemplo, *Carta Porte*), bastará con registrar un nuevo `CartaPorteProcessor` que inyecte su respectivo `DS_CARTAPORTE` al mapa de parámetros, sin alterar los reportes de Factura o Nómina existentes.

---

## 🚀 Estructura de Recursos (Classpath)

Los archivos de diseño visual compilados deben guardarse estrictamente en la sección de recursos del proyecto. **Es mandatorio incluir los archivos `.jasper` ya compilados** para optimizar el rendimiento y evitar la compilación en tiempo de ejecución:

```text
cfdi-reader/
└── src/
    └── main/
        └── resources/
            └── reports/
                ├── factura/v40
                │           ├── principal.jasper
                │           ├── principal.jrxml
                │           ├── subreporte_emisor.jasper
                │           ├── subreporte_emisor.jrxml
                │           └── ...
                ├── pagos/v20
                │         ├── principal.jasper
                │         ├── principal.jrxml
                │         ├── subreporte_impuestos.jasper
                │         ├── subreporte_impuestos.jrxml
                │         └── ...
                └── nomina/v12
                           ├── principal.jasper
                           ├── principal.jrxml
                           └── ...
```

---

## 💻 Clase de Orquestación e Impresión (`FacturaJR`)

A continuación, se detalla la implementación de la clase encargada de coordinar todo el flujo. Recibe el XML de forma abstracta a través de un `InputStream`, invoca al motor de lectura de negocio y retorna el documento final en un flujo binario compatible con cualquier entorno.

```java
package com.tuempresa.cfdi.exporter;

import com.tuempresa.cfdi.model.CFDIPrintWrapper;
import com.tuempresa.cfdi.service.CFDIReaderService;
import mx.gob.sat.cfdi.v40.Comprobante;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Componente unificado para la conversión de XML CFDI 4.0 a flujos binarios PDF.
 */
public class FacturaJR {

    private static final Logger logger = Logger.getLogger(FacturaJR.class.getName());
    private final CFDIReaderService readerService;

    public FacturaJR() {
        this.readerService = new CFDIReaderService();
    }

    /**
     * Procesa un flujo XML en memoria, ejecuta su lógica interna según el tipo
     * de comprobante y genera el PDF resultante utilizando los formatos embebidos.
     *
     * @param xmlStream Flujo de datos en memoria del XML del CFDI.
     * @return Arreglo de bytes (byte[]) con el PDF generado listo para su distribución.
     * @throws Exception Si ocurre un fallo en el parseo, reglas de negocio o compilación visual.
     */
    public byte[] generarFormatoImpreso(InputStream xmlStream) throws Exception {
        logger.info("Iniciando conversión de CFDI a formato binario PDF...");

        // 1. Ejecutar motor de lectura y procesamiento de negocio (Factory / Strategy)
        // Este paso realiza el unmarshal de JAXB y delega a su respectivo Processor.
        CFDIPrintWrapper wrapper = readerService.ejecutarCargaJR(xmlStream);
        
        Comprobante comprobanteRaiz = wrapper.getComprobante();
        Map<String, Object> parametrosJasper = wrapper.getJasperParameters();

        // 2. Resolver y extraer la ruta del reporte correspondiente (Inyectado por el Processor)
        String subreportDir = (String) parametrosJasper.get("SUBREPORT_DIR");
        if (subreportDir == null) {
            throw new IllegalStateException("El procesador asignado no definió un directorio de reportes válido.");
        }
        
        String rutaReportePrincipal = subreportDir + "principal.jasper";
        logger.info("Buscando formato embebido en la ruta: " + rutaReportePrincipal);

        // 3. Obtener el archivo compilado .jasper desde los recursos internos del JAR
        InputStream jasperStream = getClass().getClassLoader().getResourceAsStream(rutaReportePrincipal);
        if (jasperStream == null) {
            throw new FileNotFoundException("No se encontró el formato predefinido en el Classpath: " + rutaReportePrincipal);
        }

        // 4. Preparar la fuente de datos principal (El Comprobante Raíz)
        List<Comprobante> listaRaiz = Collections.singletonList(comprobanteRaiz);
        JRBeanCollectionDataSource mainDataSource = new JRBeanCollectionDataSource(listaRaiz);

        try {
            // 5. Inyectar los datos en el lienzo de JasperReports
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperStream, parametrosJasper, mainDataSource);

            // 6. Exportar el lienzo procesado a un arreglo de bytes crudo (PDF)
            logger.info("Exportando reporte a mapa de bytes...");
            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fallo crítico durante el llenado o exportación del PDF en JasperReports", e);
            throw e;
        }
    }
}
```

---

## 🔌 Consumo desde Proyectos Externos

Cualquier microservicio o proyecto intermedio puede consumir esta librería simplemente instanciando `FacturaJR` de la siguiente manera:

```java
// Código ejemplo en un proyecto API Web o Consumidor externo
InputStream streamXmlDesdeProveedor = ...; 

FacturaJR exporter = new FacturaJR();
byte[] pdfBytes = exporter.generarFormatoImpreso(streamXmlDesdeProveedor);

// El arreglo de bytes está listo para responderse en un HTTP Response, 
// enviarse por correo, o guardarse en un FileSystem/S3
```
