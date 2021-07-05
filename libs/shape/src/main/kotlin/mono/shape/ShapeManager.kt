package mono.shape

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mono.livedata.LiveData
import mono.livedata.MutableLiveData
import mono.shape.command.AddShape
import mono.shape.command.Command
import mono.shape.command.GroupShapes
import mono.shape.command.RemoveShape
import mono.shape.command.Ungroup
import mono.shape.serialization.AbstractSerializableShape
import mono.shape.serialization.SerializableGroup
import mono.shape.shape.AbstractShape
import mono.shape.shape.Group

/**
 * A model class which contains all shapes of the app and also defines all shape handling logics.
 */
class ShapeManager {
    var root: Group = Group(parentId = null)
        private set
    private var allShapeMap: MutableMap<String, AbstractShape> = mutableMapOf(root.id to root)

    /**
     * Reflect the version of the root through live data. The other components are able to observe
     * this version to decide update internally.
     */
    private val versionMutableLiveData: MutableLiveData<Int> = MutableLiveData(root.version)
    val versionLiveData: LiveData<Int> = versionMutableLiveData

    init {
        replaceRoot(root)
    }

    /**
     * Replace [root] with [newRoot].
     * This also wipe current stored shapes with shapes in new root.
     */
    fun replaceRoot(newRoot: Group) {
        val currentVersion = root.version
        root = newRoot

        allShapeMap = createAllShapeMap(newRoot)

        versionMutableLiveData.value =
            if (currentVersion == newRoot.version) currentVersion - 1 else newRoot.version
    }

    private fun createAllShapeMap(group: Group): MutableMap<String, AbstractShape> {
        val map: MutableMap<String, AbstractShape> = mutableMapOf()
        map[group.id] = group
        createAllShapeMapRecursive(group, map)
        return map
    }

    private fun createAllShapeMapRecursive(group: Group, map: MutableMap<String, AbstractShape>) {
        for (shape in group.items) {
            map[shape.id] = shape
            if (shape is Group) {
                createAllShapeMapRecursive(group, map)
            }
        }
    }

    fun execute(command: Command) {
        val affectedParent = command.getDirectAffectedParent(this) ?: return
        val allAncestors = affectedParent.getAllAncestors()
        val currentVersion = affectedParent.version

        command.execute(this, affectedParent)

        if (currentVersion == affectedParent.version && affectedParent.id in allShapeMap) {
            return
        }
        for (parent in allAncestors) {
            parent.update { true }
        }
        versionMutableLiveData.value = root.version
    }

    internal fun getGroup(shapeId: String?): Group? =
        if (shapeId == null) root else allShapeMap[shapeId] as? Group

    fun getShape(shapeId: String): AbstractShape? = allShapeMap[shapeId]

    internal fun register(shape: AbstractShape) {
        allShapeMap[shape.id] = shape
    }

    internal fun unregister(shape: AbstractShape) {
        allShapeMap.remove(shape.id)
    }

    private fun Group.getAllAncestors(): List<Group> {
        val result = mutableListOf<Group>()
        var parent = allShapeMap[parentId] as? Group
        while (parent != null) {
            result.add(parent)
            parent = allShapeMap[parent.parentId] as? Group
        }
        return result
    }
}

fun ShapeManager.add(shape: AbstractShape) = execute(AddShape(shape))

fun ShapeManager.remove(shape: AbstractShape?) {
    if (shape != null) {
        execute(RemoveShape(shape))
    }
}

fun ShapeManager.group(sameParentShapes: List<AbstractShape>) =
    execute(GroupShapes(sameParentShapes))

fun ShapeManager.ungroup(group: Group) = execute(Ungroup(group))

fun ShapeManager.toJson(): String = Json.encodeToString(root.toSerializableShape())

fun ShapeManager.replaceWithJson(jsonString: String): Boolean = try {
    val serializableGroup =
        Json.decodeFromString<AbstractSerializableShape>(jsonString) as SerializableGroup
    val root = Group(serializableGroup, parentId = null)
    replaceRoot(root)
    true
} catch (e: Exception) {
    console.error("Error while restoring shapes")
    console.error(e)

    false
}
