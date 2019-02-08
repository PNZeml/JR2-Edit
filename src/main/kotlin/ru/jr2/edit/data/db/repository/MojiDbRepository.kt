package ru.jr2.edit.data.db.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import ru.jr2.edit.EditApp
import ru.jr2.edit.data.db.table.ComponentKanjiTable
import ru.jr2.edit.data.db.table.MojiTable
import ru.jr2.edit.domain.entity.MojiEntity
import ru.jr2.edit.domain.model.Moji
import ru.jr2.edit.util.KotlinLoggingSqlLogger

class MojiDbRepository(
    private val db: Database = EditApp.instance.db
) {
    fun getById(id: Int): Moji = transaction(db) {
        addLogger(KotlinLoggingSqlLogger)
        return@transaction Moji.fromEntity(MojiEntity[id])
    }

    fun getById(vararg id: Int): List<Moji> = transaction(db) {
        return@transaction id.map {
            Moji.fromEntity(MojiEntity[it])
        }
    }

    fun getAll(): List<Moji> = transaction(db) {
        return@transaction MojiEntity.all().map { Moji.fromEntity(it) }
    }

    fun getComponentsOfMoji(mojiId: Int): List<Moji> = transaction(db) {
        addLogger(KotlinLoggingSqlLogger)
        val componentAlias = ComponentKanjiTable.alias("component_moji")
        return@transaction MojiTable
            .innerJoin(
                componentAlias,
                { MojiTable.id },
                { componentAlias[ComponentKanjiTable.mojiComponentId] }
            )
            .slice(MojiTable.columns)
            .select {
                componentAlias[ComponentKanjiTable.moji] eq MojiEntity[mojiId].id
            }
            .map {
                Moji.fromEntity(MojiEntity.wrapRow(it))
            }
    }

    // TODO: Придумать мхеанизм поиска, перевод каны в романджи?
    fun getBySearchQuery(query: String): List<Moji> = transaction(db) {
        return@transaction MojiTable
            .select {
                MojiTable.basicInterpretation.upperCase() like "%$query%".toUpperCase()
            }
            .map {
                Moji.fromEntity(MojiEntity.wrapRow(it))
            }
    }

    fun insert(moji: Moji): Moji = transaction(db) {
        val newMoji = MojiEntity.new {
            value = moji.value
            strokeCount = moji.strokeCount
            kunReading = moji.kunReading
            onReading = moji.onReading
            basicInterpretation = moji.basicInterpretation
            jlptLevel = moji.jlptLevel
            mojiType = moji.mojiType
        }
        return@transaction Moji.fromEntity(newMoji)
    }

    fun insertUpdate(moji: Moji): Moji = transaction(db) {
        addLogger(KotlinLoggingSqlLogger)
        return@transaction MojiEntity.findById(moji.id)?.run {
            value = moji.value
            strokeCount = moji.strokeCount
            kunReading = moji.kunReading
            onReading = moji.onReading
            basicInterpretation = moji.basicInterpretation
            jlptLevel = moji.jlptLevel
            mojiType = moji.mojiType
            getById(moji.id)
        } ?: insert(moji)
    }

    @Suppress("NAME_SHADOWING")
    fun insertUpdateMojiComponent(moji: Moji, components: List<Moji>) = transaction(db) {
        val moji = insertUpdate(moji)
        /*
        * Поскольку в Exposed нет (или пока нет) массового обновления записей,
        * то перед созданием приходится полностью удалять компоненты
        */
        ComponentKanjiTable.deleteWhere {
            ComponentKanjiTable.moji eq MojiEntity[moji.id].id
        }
        var orderIdx = -1
        ComponentKanjiTable.batchInsert(components) {
            this[ComponentKanjiTable.moji] = MojiEntity[moji.id].id
            this[ComponentKanjiTable.mojiComponentId] = MojiEntity[it.id].id
            this[ComponentKanjiTable.order] = orderIdx++
        }
    }

    fun deleteById(id: Int) = transaction(db) {
        MojiEntity[id].delete()
    }
}