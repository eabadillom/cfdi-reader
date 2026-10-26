package com.ferbo.cfdi.v40.model;

public enum TipoCFDI {
    FACTURA,
    COMPLEMENTO_PAGO,
    NOMINA;

    /**
     * Determina el tipo de CFDI basado en los datos del Comprobante JAXB
     */
    public static TipoCFDI detectar(mx.gob.sat.cfdi.v40.Comprobante comprobante) {
        String tipo = comprobante.getTipoDeComprobante().value(); // "I", "E", "P", "N", "T"
        
        if ("P".equals(tipo)) {
            return COMPLEMENTO_PAGO;
        } else if ("N".equals(tipo)) {
            return NOMINA;
        } else {
            // "I" (Ingreso) o "E" (Egreso) sin complementos de nómina/pago se tratan como factura
            return FACTURA;
        }
    }
}
