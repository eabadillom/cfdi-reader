package com.ferbo.cfdi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ferbo.cfdi.readers.CFDIReader;
import com.ferbo.cfdi.readers.ComplementoPagoReader;


@Deprecated
public class ReadCFDI40 {
	
	private static Logger log = LogManager.getLogger(ReadCFDI40.class);
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Uso: java CFDIReader </ruta/al/cfdi.xml>");
			System.exit(1);
		}
		
		String rutaXml = args[0];
        log.info("Leyendo CFDI: {}", rutaXml);
		
        CFDIReader cfdi = new ComplementoPagoReader();
        cfdi.read(rutaXml);
    }
}
