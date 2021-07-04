/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;

/**
 *
 * @author Al Badr
 */
public class Rule {

    private int source_cdr_li_id;
    private int destination_cdr_li_id;
    private int rule_id;


    public int getSource_cdr_li_id() {
        return source_cdr_li_id;
    }

    public void setSource_cdr_li_id(int source_cdr_li_id) {
        this.source_cdr_li_id = source_cdr_li_id;
    }

    public int getDestination_cdr_li_id() {
        return destination_cdr_li_id;
    }

    public void setDestination_cdr_li_id(int destination_cdr_li_id) {
        this.destination_cdr_li_id = destination_cdr_li_id;
    }

    public int getRule_id() {
        return rule_id;
    }

    public void setRule_id(int rule_id) {
        this.rule_id = rule_id;
    }



}
