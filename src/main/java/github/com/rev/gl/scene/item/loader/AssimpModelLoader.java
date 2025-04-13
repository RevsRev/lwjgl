package github.com.rev.gl.scene.item.loader;

import github.com.rev.gl.scene.item.Model;
import github.com.rev.gl.scene.item.Mesh;
import github.com.rev.gl.texture.LayerManager;
import github.com.rev.gl.texture.image.GlImageTextureBinding;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMaterialProperty;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class AssimpModelLoader implements ModelLoader {

    @Override
    public Model load(String modelPath, final LayerManager layerManager) {
        String modelFolder = new File(modelPath).getParent();
        AIScene aiScene = Assimp.aiImportFile(modelPath, Assimp.aiProcess_Triangulate /*| Assimp.aiProcess_FlipUVs*/);

        if (aiScene == null
                || ((aiScene.mFlags() & Assimp.AI_SCENE_FLAGS_INCOMPLETE) == Assimp.AI_SCENE_FLAGS_INCOMPLETE)
                || aiScene.mRootNode() == null) {
            System.out.println("ERROR::ASSIMP:: " + Assimp.aiGetErrorString());
            return null;
        }

        Model model = new Model();
        processNode(model, aiScene.mRootNode(), aiScene, layerManager, modelFolder);
        return model;
    }

    private void processNode(Model model, final AINode aiNode, final AIScene scene, LayerManager layerManager, String modelFolder) {

        PointerBuffer scenePointer = scene.mMeshes();
        IntBuffer meshesBuffer = aiNode.mMeshes();
        for (int i = 0; i < aiNode.mNumMeshes(); i++) {
            long address = scenePointer.get(meshesBuffer.get());
            AIMesh mesh = AIMesh.create(address);
            model.addMesh(processMesh(mesh, scene, layerManager, modelFolder));
        }

        PointerBuffer childrenBuffer = aiNode.mChildren();
        for (int i = 0; i < aiNode.mNumChildren(); i++) {
            AINode child = AINode.create(childrenBuffer.get());
            processNode(model, child, scene, layerManager, modelFolder);
        }
    }

    private Mesh processMesh(final AIMesh mesh, final AIScene scene, LayerManager layerManager, String modelFolder) {
        final AIVector3D.Buffer verticesBuffer = mesh.mVertices();
        final AIVector3D.Buffer normalsBuffer = mesh.mNormals();
        final AIVector3D.Buffer texCoordBuffer = mesh.mTextureCoords(0);

        float[] arr = new float[8 * mesh.mNumVertices()];

        for (int i = 0; i < mesh.mNumVertices(); i++) {
            final AIVector3D vertices = verticesBuffer.get(i);
            final AIVector3D normals = normalsBuffer.get(i);

            arr[8 * i] = vertices.x();
            arr[8 * i + 1] = vertices.y();
            arr[8 * i + 2] = vertices.z();
            arr[8 * i + 3] = normals.x();
            arr[8 * i + 4] = normals.y();
            arr[8 * i + 5] = normals.z();

            if (texCoordBuffer == null || texCoordBuffer.capacity() == 0) {
                arr[8 * i + 6] = 0.0f;
                arr[8 * i + 7] = 0.0f;
            } else {
                final AIVector3D textureCoords = texCoordBuffer.get(i);
                arr[8 * i + 6] = textureCoords.x();
                arr[8 * i + 7] = textureCoords.y();
            }
        }

        // Process indices
        //TODO - Can probably optimise this...
        AIFace.Buffer meshFacesBuffer = mesh.mFaces();
        final List<Integer> indices = new ArrayList<>();
        int indicesSize = 0;
        for (int i = 0; i < mesh.mNumFaces(); i++) {
            AIFace aiFace = meshFacesBuffer.get();
            indicesSize += aiFace.mNumIndices();
        }
        final int[] indicesArr = new int[indicesSize];
        int start = 0;
        meshFacesBuffer = mesh.mFaces();
        for (int i = 0; i < mesh.mNumFaces(); i++) {
            AIFace aiFace = meshFacesBuffer.get();
            IntBuffer faceIndicesBuffer = aiFace.mIndices();
            for (int j = 0; j < aiFace.mNumIndices(); j++) {
                indicesArr[start + j] = faceIndicesBuffer.get();
            }
            start += aiFace.mNumIndices();
        }

        // Materials (i.e. textures)
        int materialIndex = mesh.mMaterialIndex();
        PointerBuffer sceneMaterialsBuffer = scene.mMaterials();
        long address = sceneMaterialsBuffer.get(materialIndex);
        AIMaterial material = AIMaterial.create(address);

        Mesh.Builder simpleItemBuilder = new Mesh.Builder(arr,
                "scene/shaders/vertex/simple_item.vert",
                "scene/shaders/fragment/simple_item_simple_material.frag")
//                .addAmbientTexture("src/main/resources/scene/textures/container2.png")
                .withIndices(indicesArr.length == 0 ? null : indicesArr);

        loadMaterialTextures(material, scene, simpleItemBuilder, modelFolder);

        return simpleItemBuilder.build(layerManager);
    }

    private static final Map<Integer, BiConsumer<Mesh.Builder, String>> SUPPORTED_TEXTURES = Map.of(
            Assimp.aiTextureType_AMBIENT, Mesh.Builder::addAmbientTexture,
            Assimp.aiTextureType_DIFFUSE, Mesh.Builder::addDiffuseTexture/*,
            Assimp.aiTextureType_SPECULAR, SimpleItem.Builder::addSpecularTexture*/);

    private List<GlImageTextureBinding> loadMaterialTextures(AIMaterial material, AIScene scene,
                                                             Mesh.Builder simpleItemBuilder, String modelFolder) {
        PointerBuffer properties = material.mProperties();

        for (int i = 0; i < properties.remaining(); i++) {
            AIMaterialProperty prop = AIMaterialProperty.create(properties.get(i));

            int semantic = prop.mSemantic();
            if (SUPPORTED_TEXTURES.containsKey(semantic) && prop.mType() == Assimp.aiPTI_String && "$tex.file".equals(prop.mKey().dataString())) {
                ByteBuffer aiStringBytes = prop.mData();
                String filePath = modelFolder + "/" + readString(aiStringBytes);
                SUPPORTED_TEXTURES.get(semantic).accept(simpleItemBuilder, filePath);
//                SUPPORTED_TEXTURES.get(semantic).accept(simpleItemBuilder, "src/main/resources/scene/textures/container2.png");
            }
        }
        return null;
    }

    // There's probably a better way to do this, but this works for now :)
    private String readString(final ByteBuffer aiStringBytes) {
        int len = aiStringBytes.remaining();
        if (len < 5) {
            throw new RuntimeException("Trying to parse a ByteBuffer as an AIString, but it is malformed.", new MalformedInputException(len));
        }
        if (len == 5) {
            return "";
        }

        final int strStart = 4;
        final int strLen = len - 5;
        final byte[] strBytes = new byte[strLen];
        for (int i = 0; i < strLen; i++) {
            strBytes[i] = aiStringBytes.get(strStart + i);
        }
        return new String(strBytes, StandardCharsets.UTF_8);
    }
}
