package edu.duke.ra.core.result;

import java.util.ArrayList;
import java.util.List;

import edu.duke.ra.core.RAException;

public class HelpQueryResult extends RawStringQueryResult {
    private static final String query = "\\help;\n";
    private static final String helpMessage = ""
            + "Terminate your commands or expressions by \";\" \n"
            + "\n"
            + "Commands: \n"
            + "\\help: print this message \n"
            + "\\quit: exit ra \n"
            + "\\list: list all relations in the database \n"
            + "\\sqlexec_{STATEMENT}: execute SQL in the database \n"
            + "\n"
            + "Relational algebra expressions: \n"
            + "R: relation named by R \n"
            + "\\select_{COND} EXP: selection over an expression \n"
            + "\\project_{ATTR_LIST} EXP: projection of an expression \n"
            + "EXP_1 \\join EXP_2: natural join between two expressions \n"
            + "EXP_1 \\join_{COND} EXP_2: theta-join between two expressions \n"
            + "EXP_1 \\cross EXP_2: cross-product between two expressions \n"
            + "EXP_1 \\union EXP_2: union between two expressions \n"
            + "EXP_1 \\diff EXP_2: difference between two expressions \n"
            + "EXP_1 \\intersect EXP_2: intersection between two expressions \n"
            + "\\rename_{NEW_ATTR_NAME_LIST} EXP: rename all attributes of an expression \n"
            + "\n"
            ;
    private static final List<RAException> errors = new ArrayList<>();

    public HelpQueryResult() {
        super(query, helpMessage, errors);
    }
}
