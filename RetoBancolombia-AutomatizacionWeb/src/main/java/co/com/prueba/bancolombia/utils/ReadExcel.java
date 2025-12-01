package co.com.prueba.bancolombia.utils;

import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReadExcel {
    private ReadExcel() {
    }

    public static List<String> readRow(String id, String archive, String sheet) throws FilloException {
        Fillo fillo = new Fillo();
        File file = new File("src/test/resources/files/" + archive);
        String archivePath = file.getAbsolutePath();
        Connection connection = fillo.getConnection(archivePath);
        String strQuery = "select * from " + sheet + " where ID = " + id;

        Recordset recordset = connection.executeQuery(strQuery);
        List<String> values = new ArrayList<>();
        while (recordset.next()) {
            for (int i = 0; i < recordset.getFieldNames().size() - 1; i++) {
                values.add(recordset.getField(i + 1).value());
            }
        }

        recordset.close();
        connection.close();
        return values;
    }
}
