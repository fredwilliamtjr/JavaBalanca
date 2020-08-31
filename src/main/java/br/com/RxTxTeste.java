package br.com;

import br.com.rxtx.ComunicacaoSerialUtils;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class RxTxTeste {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        String buscaBalanca = ComunicacaoSerialUtils.buscaRapidaBalanca(true, false);
        System.out.println("buscaBalanca : " + buscaBalanca);

//        String buscaDetalhadaBalanca = ComunicacaoSerialUtils.buscaDetalhadaBalanca(true, false);
//        System.out.println("buscaDetalhadaBalanca : " + buscaDetalhadaBalanca);

        int dataBits = SerialPort.DATABITS_8;
        int stopBits = SerialPort.STOPBITS_2;
        int parity = SerialPort.PARITY_NONE;
        int flow = SerialPort.FLOWCONTROL_NONE;

        CommPortIdentifier commPortIdentifier = ComunicacaoSerialUtils.identificaPorta("COM1", true);
        ComunicacaoSerialUtils comunicacaoSerialUtils1 = new ComunicacaoSerialUtils();
        String buscaPeso = comunicacaoSerialUtils1.buscaPeso(commPortIdentifier, 2400, 500, dataBits, stopBits, parity, flow, false, 5);
        System.out.println("buscaPeso : " + buscaPeso);


    }

}
