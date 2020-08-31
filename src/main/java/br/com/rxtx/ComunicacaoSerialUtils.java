package br.com.rxtx;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class ComunicacaoSerialUtils implements SerialPortEventListener {

    private int timeout;
    private boolean log;

    private CommPortIdentifier commPortIdentifier;
    private SerialPort serialPort;
    private OutputStream saida;
    private InputStream entrada;
    private String retornoPorta;

    private static String retornaSomenteNumeros(String str) {
        if (str != null) {
            return str.replaceAll("[^0123456789]", "");
        } else {
            return "";
        }
    }

    public static List<CommPortIdentifier> listarPostas() {
        Enumeration portIdentifiers = CommPortIdentifier.getPortIdentifiers();
        ArrayList list = Collections.list(portIdentifiers);
        List<CommPortIdentifier> commPortIdentifierList = new ArrayList<>();
        for (Object o : list) {
            CommPortIdentifier commPortIdentifier = (CommPortIdentifier) o;
            commPortIdentifierList.add(commPortIdentifier);
        }
        return commPortIdentifierList;
    }

    public static CommPortIdentifier identificaPorta(String porta, boolean log) throws Exception {
        try {
            CommPortIdentifier commPortIdentifier = CommPortIdentifier.getPortIdentifier(porta);
            if (commPortIdentifier == null) {
                throw new Exception("identificaPorta, porta não encontrada");
            } else {
                if (log) {
                    System.out.println("identificaPorta : " + commPortIdentifier.getName());
                }
                return commPortIdentifier;
            }
        } catch (Exception e) {
            throw new Exception("identificaPorta, erro : " + e);
        }
    }

    public void abrirPorta(CommPortIdentifier commPortIdentifier, int baudrate, int timeout, int dataBits, int stopBits, int parity, int flowControl, boolean log) throws Exception {
        this.commPortIdentifier = commPortIdentifier;
        this.timeout = timeout;
        try {
            serialPort = (SerialPort) this.commPortIdentifier.open("ComunicacaoSerialUtils", this.timeout);
            serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
            serialPort.setFlowControlMode(flowControl);
            if (log) {
                System.out.println("abrirPorta, porta " + commPortIdentifier.getName() + " aberta!");
            }
        } catch (Exception e) {
            throw new Exception("abrirPorta, erro : " + e);
        }
    }

    public void enviarComando(int msg) throws Exception {
        try {
            saida = serialPort.getOutputStream();
            if (log) {
                System.out.println("enviarComando, FLUXO OK!");
            }
        } catch (Exception e) {
            throw new Exception("enviarComando, erro : " + e);
        }
        try {
            if (log) {
                System.out.println("enviarComando, enviando um byte para " + commPortIdentifier.getName());
                System.out.println("enviarComando, enviando : " + msg);
            }
            saida.write(msg);
            Thread.sleep(timeout);
            saida.flush();
        } catch (Exception e) {
            throw new Exception("enviarComando, Houve um erro durante o envio : " + e);
        }
    }

    public void serialEvent(SerialPortEvent ev) {
        StringBuffer bufferLeitura = new StringBuffer();

        int novoDado = 0;

        switch (ev.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE:
                while (novoDado != -1) {
                    try {
                        novoDado = entrada.read();

                        if (novoDado == -1) {
                            break;
                        }

                        if ('\r' == (char) novoDado) {
                            bufferLeitura.append('\n');
                        } else {
                            bufferLeitura.append((char) novoDado);
                        }
                    } catch (IOException ioe) {
                        retornoPorta = null;
                        System.out.println("serialEvent, erro de leitura serial: " + ioe);
                    }
                }
                retornoPorta = new String(bufferLeitura);
                if (log) {
                    System.out.println("serialEvent, retorno: " + retornoPorta);
                }
                break;
        }

    }

    public String lerDados() throws Exception {
        try {
            entrada = serialPort.getInputStream();
        } catch (Exception e) {
            throw new Exception("lerDados, erro de stream: " + e);
        }
        try {
            serialPort.addEventListener(this);
        } catch (Exception e) {
            throw new Exception("lerDados, erro de listener: " + e);
        }
        serialPort.notifyOnDataAvailable(true);
        try {
            Thread.sleep(timeout);
        } catch (Exception e) {
            throw new Exception("lerDados, erro de Thred: " + e);
        }
        return retornoPorta;
    }

    public void fecharPorta() throws Exception {
        try {
            serialPort.close();
            retornoPorta = null;
            if (log) {
                System.out.println("fecharCom, porta fechada!");
            }
        } catch (Exception e) {
            throw new Exception("fecharCom, erro fechando porta: " + e);
        }

    }

    private BigDecimal ajustaDecimais(String valor) {

        BigDecimal retorno = BigDecimal.ZERO;

        try {
            BigDecimal bigDecimal = new BigDecimal(valor);
            retorno = bigDecimal;
        } catch (Exception e) {
            try {
                valor = valor.replace(".", "").replace(",", ".");
                BigDecimal bigDecimal = new BigDecimal(valor);
                retorno = bigDecimal;
            } catch (Exception ignored) {
            }
        }

        return retorno;

    }

    public static String buscaRapidaBalanca(boolean logBusca, boolean logErro) {

        if (logBusca) {
            System.out.println("Iniciou : " + LocalDateTime.now());
        }

        List<CommPortIdentifier> commPortIdentifierList = listarPostas();

        List<Integer> baudrateList = Arrays.asList(1200, 1800, 2400, 4800, 7200, 9600, 14400, 19200, 38400, 57600, 115200, 128000);
        List<Integer> dataBitsList = Collections.singletonList(8);
        List<Integer> stopBitsList = Collections.singletonList(2);
        List<Integer> parityList = Collections.singletonList(0);
        List<Integer> flowControlList = Collections.singletonList(0);

        for (CommPortIdentifier portIdentifier : commPortIdentifierList) {
            for (Integer integer : baudrateList) {
                for (Integer integer1 : dataBitsList) {
                    for (Integer integer2 : stopBitsList) {
                        for (Integer integer3 : parityList) {
                            for (Integer integer4 : flowControlList) {
                                try {
                                    ComunicacaoSerialUtils comunicacaoSerialUtils = new ComunicacaoSerialUtils();
                                    String retorno = "port: " + portIdentifier.getName() + ", baudrate: " + integer + ", dataBits: " + integer1 + ", stopBits: " + integer2 + ", parity: " + integer3 + ", flowControl: " + integer4;
                                    if (logBusca) {
                                        System.out.println("Testando : " + retorno);
                                    }
                                    String buscaPeso = comunicacaoSerialUtils.buscaPeso(portIdentifier, integer, 500, integer1, integer2, integer3, integer4, logErro, 5);
                                    BigDecimal bigDecimal = new BigDecimal(buscaPeso);
                                    if (bigDecimal.signum() != 0) {
                                        if (logBusca) {
                                            System.out.println("Terminou : " + LocalDateTime.now());
                                        }
                                        return retorno;
                                    }
                                } catch (Exception e) {
                                    if (logErro) {
                                        System.out.println(e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (logBusca) {
            System.out.println("Terminou : " + LocalDateTime.now());
        }

        return "Não encontrado peso superior a 0!";
    }

    public static String buscaDetalhadaBalanca(boolean logBusca, boolean logErro) {

        if (logBusca) {
            System.out.println("Iniciou : " + LocalDateTime.now());
        }

        List<CommPortIdentifier> commPortIdentifierList = listarPostas();

        List<Integer> baudrateList = Arrays.asList(75, 110, 134, 150, 300, 600, 1200, 1800, 2400, 4800, 7200, 9600, 14400, 19200, 38400, 57600, 115200, 128000);
        List<Integer> dataBitsList = Arrays.asList(4, 5, 6, 7, 8);
        List<Integer> stopBitsList = Arrays.asList(1, 2, 3);
        List<Integer> parityList = Arrays.asList(0, 1, 2, 3, 4);
        List<Integer> flowControlList = Arrays.asList(0, 1, 2, 4, 8);

        for (CommPortIdentifier portIdentifier : commPortIdentifierList) {
            for (Integer integer : baudrateList) {
                for (Integer integer1 : dataBitsList) {
                    for (Integer integer2 : stopBitsList) {
                        for (Integer integer3 : parityList) {
                            for (Integer integer4 : flowControlList) {
                                try {
                                    ComunicacaoSerialUtils comunicacaoSerialUtils = new ComunicacaoSerialUtils();
                                    String retorno = "port: " + portIdentifier.getName() + ", baudrate: " + integer + ", dataBits: " + integer1 + ", stopBits: " + integer2 + ", parity: " + integer3 + ", flowControl: " + integer4;
                                    if (logBusca) {
                                        System.out.println("Testando : " + retorno);
                                    }
                                    String buscaPeso = comunicacaoSerialUtils.buscaPeso(portIdentifier, integer, 500, integer1, integer2, integer3, integer4, logErro, 5);
                                    BigDecimal bigDecimal = new BigDecimal(buscaPeso);
                                    if (bigDecimal.signum() != 0) {

                                        if (logBusca) {
                                            System.out.println("Terminou : " + LocalDateTime.now());
                                        }

                                        return retorno;
                                    }
                                } catch (Exception e) {
                                    if (logErro) {
                                        System.out.println(e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (logBusca) {
            System.out.println("Terminou : " + LocalDateTime.now());
        }

        return "Não encontrado peso superior a 0!";
    }

    public String buscaPeso(CommPortIdentifier commPortIdentifier, int baudrate, int timeout, int dataBits, int stopBits, int parity, int flowControl, boolean log, int comando) throws Exception {
        abrirPorta(commPortIdentifier, baudrate, timeout, dataBits, stopBits, parity, flowControl, log);
        enviarComando(comando);
        String lerDados = lerDados();
        String retornaSomenteNumeros = retornaSomenteNumeros(lerDados);
        fecharPorta();
        return retornaSomenteNumeros;
    }

}
