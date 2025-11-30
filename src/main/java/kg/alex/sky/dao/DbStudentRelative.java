/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.sky.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import kg.alex.sky.MyVaadinUI;
import kg.alex.sky.domain.Definition;
import kg.alex.sky.utils.Settings;
import kg.alex.sky.domain.StudentRelative;
import kg.alex.sky.i18n.Messages;
import kg.alex.sky.ui.StudentDefinitionView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

/**
 * @author alex
 */
public class DbStudentRelative extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbStudentRelative.class);

    public DbStudentRelative() throws Exception {
        super();
    }

    public IndexedContainer execSQL_St_Rel(MyVaadinUI myUi, int stud_id,
                                           StudentDefinitionView dw) throws SQLException {

        String sql = "SELECT sr.id, sr.student_id, sr.fullname, sr.work_place_id, "
                + "sr.phone, sr.address_line, sr.address_id, sr.address_id, sr.passport, "
                + "sr.is_main, sr.relatives_id, sr.inn "
                + "FROM student_relatives as sr WHERE sr.student_id = ?";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        ResultSet result = stat.executeQuery();

        IndexedContainer container = dw.prepareRelativesContainer();

        while (result.next()) {
            // В ResultSet имя колонки будет "id", а не "sr.id"
            String id = result.getString("id");
            boolean isMain = result.getInt("sr.is_main") == 1;

            Item item = container.addItem(id);

            // Кнопка удаления
            item.getItemProperty(Settings.button).setValue(
                    dw.createButton(
                            myUi.getMessage(Messages.DeleteButton),
                            id,
                            Settings.dbStudentRelatives,
                            FontAwesome.MINUS_SQUARE
                    )
            );

            // Общие поля, различается только editable по isMain
            item.getItemProperty(myUi.getMessage(Messages.FullName)).setValue(
                    dw.createTextField(
                            result.getString("sr.fullname"),
                            myUi.getMessage(Messages.FullName),
                            id,
                            false,
                            isMain
                    )
            );

            item.getItemProperty(myUi.getMessage(Messages.Address)).setValue(
                    dw.createTextField(
                            result.getString("sr.address_line"),
                            myUi.getMessage(Messages.Address),
                            id,
                            true,
                            isMain
                    )
            );

            item.getItemProperty(myUi.getMessage(Messages.Phone)).setValue(
                    dw.createTextField(
                            result.getString("sr.phone"),
                            myUi.getMessage(Messages.Phone),
                            id,
                            true,
                            isMain
                    )
            );
            final ComboBox cbWP = dw.createCombobox(
                    result.getInt("sr.work_place_id"),
                    myUi.getMessage(Messages.WorkPlace),
                    id, Settings.dbWork_placeTable,
                    false,
                    isMain
            );
            cbWP.setNewItemsAllowed(true);
            cbWP.setNewItemHandler((AbstractSelect.NewItemHandler) newItemCaption -> {
                try {
                    DbDefinition dbd = new DbDefinition();
                    dbd.connect();
                    int id1 = dbd.exec_insert(new Definition(0, newItemCaption), Settings.dbWork_placeTable, false);
                    dbd.close();
                    if (id1 != 0) {
                        for (Object next : container.getItemIds()) {
                            Item item1 = ((ComboBox) container.getContainerProperty(next,
                                    myUi.getMessage(Messages.WorkPlace)).getValue()).getContainerDataSource().addItem(id1);
                            item1.getItemProperty(myUi.getMessage(Messages.Title)).setValue(newItemCaption);
                            cbWP.setValue(id1);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            });
            item.getItemProperty(myUi.getMessage(Messages.WorkPlace)).setValue(cbWP);

            item.getItemProperty(myUi.getMessage(Messages.Passport)).setValue(
                    dw.createTextField(
                            result.getString("sr.passport"),
                            myUi.getMessage(Messages.Passport),
                            id,
                            true,
                            isMain
                    )
            );

            item.getItemProperty(myUi.getMessage(Messages.INN)).setValue(
                    dw.createTextField(
                            result.getString("sr.inn"),
                            myUi.getMessage(Messages.INN),
                            id,
                            true,
                            isMain
                    )
            );

            // Responsible чекбокс — true только для основного
            item.getItemProperty(myUi.getMessage(Messages.Responsible)).setValue(
                    dw.createCheckBox(
                            isMain,
                            myUi.getMessage(Messages.Responsible),
                            id
                    )
            );

            // Тип родственника — без изменений
            item.getItemProperty(myUi.getMessage(Messages.RelativeType)).setValue(
                    dw.createCombobox(
                            result.getInt("sr.relatives_id"),
                            myUi.getMessage(Messages.RelativeType),
                            id,
                            Settings.dbRelatives,
                            false,
                            true
                    )
            );
            ComboBox cb = dw.createCombobox(0,
                    myUi.getMessage(Messages.Locality),
                    id,
                    null,
                    false,
                    false
            );
            try {
                DbDefinition dbCon = new DbDefinition();
                dbCon.connect();
                cb.setContainerDataSource(dbCon.execAddresses(myUi));
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            cb.setValue(result.getInt("sr.address_id"));
            item.getItemProperty(myUi.getMessage(Messages.Locality)).setValue(cb);

            item.getItemProperty(Settings.crud_status).setValue(myUi.getMessage(Messages.Update));
        }

        return container;
    }


    public int exec_insert(StudentRelative r) throws SQLException {
        String sql = "INSERT INTO student_relatives (student_id, fullname, "
                + "work_place_id, phone, address_line, passport, is_main, relatives_id, address_id, inn) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, r.getStudent_id());
        stat.setString(2, r.getFullName());
        if (r.getWorkPlaceId() != 0) {
            stat.setInt(3, r.getWorkPlaceId());
        } else {
            stat.setNull(3, Types.INTEGER);
        }
        stat.setString(4, r.getPhone());
        stat.setString(5, r.getAddressLine());
        stat.setString(6, r.getPassport());
        stat.setInt(7, r.getIs_main());
        stat.setInt(8, r.getRelative_id());
        if (r.getAddressId() != 0) {
            stat.setInt(9, r.getAddressId());
        } else {
            stat.setNull(9, Types.INTEGER);
        }
        stat.setString(10, r.getINN());
        System.out.println(stat);
        return stat.executeUpdate();
    }

    public int exec_update(StudentRelative sr) throws SQLException {
        String sql = "Update student_relatives set student_id = ?, "
                + "fullname = ?, work_place_id = ?, phone = ?, address_line = ?, "
                + "passport = ?, is_main = ?, relatives_id = ?, address_id = ?, inn = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, sr.getStudent_id());
        stat.setString(2, sr.getFullName());
        if (sr.getWorkPlaceId() != 0) {
            stat.setInt(3, sr.getWorkPlaceId());
        } else {
            stat.setNull(3, Types.INTEGER);
        }
        stat.setString(4, sr.getPhone());
        stat.setString(5, sr.getAddressLine());
        stat.setString(6, sr.getPassport());
        stat.setInt(7, sr.getIs_main());
        stat.setInt(8, sr.getRelative_id());
        if (sr.getAddressId() != 0) {
            stat.setInt(9, sr.getAddressId());
        } else {
            stat.setNull(9, Types.INTEGER);
        }
        stat.setString(10, sr.getINN());
        stat.setInt(11, sr.getId());
        System.out.println(stat);
        return stat.executeUpdate();
    }

    public String exec_get_who_paid(int stud_id) throws SQLException {
        String sql = "SELECT fullname FROM student_relatives where student_id = ? and is_main = 1";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        ResultSet result = stat.executeQuery();
        String p = "";
        if (result.next()) {
            p = result.getString("fullname");
        }
        return p;
    }
}
