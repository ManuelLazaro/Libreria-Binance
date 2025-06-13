package Lib.Libreria_Binance.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BinanceResponse {
    private final boolean acceso;
    private final int estado;
    private final  String data;
    private final String mensaje;

    @Override
    public String toString(){
        return String.format("BinanceResponse{acceso=%s, estado=%d, mensaje='%s'}",
                acceso, estado, mensaje);
    }
}

