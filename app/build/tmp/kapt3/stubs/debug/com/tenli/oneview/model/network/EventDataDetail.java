package com.tenli.oneview.model.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b7\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001B\u00ff\u0001\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u0012\u0010\b\u0002\u0010\b\u001a\n\u0012\u0004\u0012\u00020\n\u0018\u00010\t\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\r\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000f\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0011\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\n\u0012\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u0016\u0012\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u0018\u0012\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u001a\u0012\b\b\u0002\u0010\u001b\u001a\u00020\u001c\u0012\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u001e\u001a\u0004\u0018\u00010\u0003\u0012\u0010\b\u0002\u0010\u001f\u001a\n\u0012\u0004\u0012\u00020 \u0018\u00010\t\u00a2\u0006\u0002\u0010!J\u000b\u0010A\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010B\u001a\u0004\u0018\u00010\u0011H\u00c6\u0003J\u000b\u0010C\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0010\u0010D\u001a\u0004\u0018\u00010\nH\u00c6\u0003\u00a2\u0006\u0002\u00106J\u000b\u0010E\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010F\u001a\u0004\u0018\u00010\u0016H\u00c6\u0003J\u000b\u0010G\u001a\u0004\u0018\u00010\u0018H\u00c6\u0003J\u000b\u0010H\u001a\u0004\u0018\u00010\u001aH\u00c6\u0003J\t\u0010I\u001a\u00020\u001cH\u00c6\u0003J\u000b\u0010J\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010K\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010L\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0011\u0010M\u001a\n\u0012\u0004\u0012\u00020 \u0018\u00010\tH\u00c6\u0003J\u000b\u0010N\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010O\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010P\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0011\u0010Q\u001a\n\u0012\u0004\u0012\u00020\n\u0018\u00010\tH\u00c6\u0003J\u000b\u0010R\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010S\u001a\u0004\u0018\u00010\rH\u00c6\u0003J\u000b\u0010T\u001a\u0004\u0018\u00010\u000fH\u00c6\u0003J\u0088\u0002\u0010U\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00032\u0010\b\u0002\u0010\b\u001a\n\u0012\u0004\u0012\u00020\n\u0018\u00010\t2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\r2\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000f2\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00112\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\n2\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u00162\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u00182\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u001a2\b\b\u0002\u0010\u001b\u001a\u00020\u001c2\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u001e\u001a\u0004\u0018\u00010\u00032\u0010\b\u0002\u0010\u001f\u001a\n\u0012\u0004\u0012\u00020 \u0018\u00010\tH\u00c6\u0001\u00a2\u0006\u0002\u0010VJ\u0013\u0010W\u001a\u00020X2\b\u0010Y\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010Z\u001a\u00020\nH\u00d6\u0001J\t\u0010[\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\u0010\u001a\u0004\u0018\u00010\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u0019\u0010\b\u001a\n\u0012\u0004\u0012\u00020\n\u0018\u00010\t\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010%R\u0019\u0010\u001f\u001a\n\u0012\u0004\u0012\u00020 \u0018\u00010\t\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010%R\u0013\u0010\u001e\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010(R\u0013\u0010\u001d\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010(R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010(R\u0013\u0010\f\u001a\u0004\u0018\u00010\r\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010,R\u0013\u0010\u0014\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010(R\u0013\u0010\u0012\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010(R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010(R\u0013\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010(R\u0018\u0010\u0017\u001a\u0004\u0018\u00010\u00188\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u00102R\u0013\u0010\u000e\u001a\u0004\u0018\u00010\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u00104R\u0015\u0010\u0013\u001a\u0004\u0018\u00010\n\u00a2\u0006\n\n\u0002\u00107\u001a\u0004\b5\u00106R\u0011\u0010\u001b\u001a\u00020\u001c\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u00109R\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b:\u0010(R\u0013\u0010\u0015\u001a\u0004\u0018\u00010\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b;\u0010<R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b=\u0010(R\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b>\u0010(R\u0013\u0010\u0019\u001a\u0004\u0018\u00010\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b?\u0010@\u00a8\u0006\\"}, d2 = {"Lcom/tenli/oneview/model/network/EventDataDetail;", "", "type", "", "image", "video", "cropImage", "name", "box", "", "", "value", "face", "Lcom/tenli/oneview/model/network/FaceData;", "plate", "Lcom/tenli/oneview/model/network/PlateData;", "attribute", "Lcom/tenli/oneview/model/network/AttributeData;", "focusVideo", "profileId", "faceCrop", "uniform", "Lcom/tenli/oneview/model/network/UniformData;", "objectData", "Lcom/tenli/oneview/model/network/ObjectData;", "weapon", "Lcom/tenli/oneview/model/network/WeaponData;", "similarity", "", "containerId", "cameraUUID", "boxes", "Lcom/tenli/oneview/model/network/BoxData;", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Lcom/tenli/oneview/model/network/FaceData;Lcom/tenli/oneview/model/network/PlateData;Lcom/tenli/oneview/model/network/AttributeData;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Lcom/tenli/oneview/model/network/UniformData;Lcom/tenli/oneview/model/network/ObjectData;Lcom/tenli/oneview/model/network/WeaponData;DLjava/lang/String;Ljava/lang/String;Ljava/util/List;)V", "getAttribute", "()Lcom/tenli/oneview/model/network/AttributeData;", "getBox", "()Ljava/util/List;", "getBoxes", "getCameraUUID", "()Ljava/lang/String;", "getContainerId", "getCropImage", "getFace", "()Lcom/tenli/oneview/model/network/FaceData;", "getFaceCrop", "getFocusVideo", "getImage", "getName", "getObjectData", "()Lcom/tenli/oneview/model/network/ObjectData;", "getPlate", "()Lcom/tenli/oneview/model/network/PlateData;", "getProfileId", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getSimilarity", "()D", "getType", "getUniform", "()Lcom/tenli/oneview/model/network/UniformData;", "getValue", "getVideo", "getWeapon", "()Lcom/tenli/oneview/model/network/WeaponData;", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Lcom/tenli/oneview/model/network/FaceData;Lcom/tenli/oneview/model/network/PlateData;Lcom/tenli/oneview/model/network/AttributeData;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Lcom/tenli/oneview/model/network/UniformData;Lcom/tenli/oneview/model/network/ObjectData;Lcom/tenli/oneview/model/network/WeaponData;DLjava/lang/String;Ljava/lang/String;Ljava/util/List;)Lcom/tenli/oneview/model/network/EventDataDetail;", "equals", "", "other", "hashCode", "toString", "app_debug"})
public final class EventDataDetail {
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String type = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String image = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String video = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String cropImage = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String name = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<java.lang.Integer> box = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String value = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.FaceData face = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.PlateData plate = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.AttributeData attribute = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String focusVideo = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer profileId = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String faceCrop = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.UniformData uniform = null;
    @com.google.gson.annotations.SerializedName(value = "object")
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.ObjectData objectData = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.WeaponData weapon = null;
    private final double similarity = 0.0;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String containerId = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String cameraUUID = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<com.tenli.oneview.model.network.BoxData> boxes = null;
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.AttributeData component10() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component12() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component13() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.UniformData component14() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.ObjectData component15() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.WeaponData component16() {
        return null;
    }
    
    public final double component17() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component18() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component19() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<com.tenli.oneview.model.network.BoxData> component20() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.Integer> component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.FaceData component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.PlateData component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.oneview.model.network.EventDataDetail copy(@org.jetbrains.annotations.Nullable()
    java.lang.String type, @org.jetbrains.annotations.Nullable()
    java.lang.String image, @org.jetbrains.annotations.Nullable()
    java.lang.String video, @org.jetbrains.annotations.Nullable()
    java.lang.String cropImage, @org.jetbrains.annotations.Nullable()
    java.lang.String name, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.Integer> box, @org.jetbrains.annotations.Nullable()
    java.lang.String value, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.FaceData face, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.PlateData plate, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.AttributeData attribute, @org.jetbrains.annotations.Nullable()
    java.lang.String focusVideo, @org.jetbrains.annotations.Nullable()
    java.lang.Integer profileId, @org.jetbrains.annotations.Nullable()
    java.lang.String faceCrop, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.UniformData uniform, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.ObjectData objectData, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.WeaponData weapon, double similarity, @org.jetbrains.annotations.Nullable()
    java.lang.String containerId, @org.jetbrains.annotations.Nullable()
    java.lang.String cameraUUID, @org.jetbrains.annotations.Nullable()
    java.util.List<com.tenli.oneview.model.network.BoxData> boxes) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
    
    public EventDataDetail(@org.jetbrains.annotations.Nullable()
    java.lang.String type, @org.jetbrains.annotations.Nullable()
    java.lang.String image, @org.jetbrains.annotations.Nullable()
    java.lang.String video, @org.jetbrains.annotations.Nullable()
    java.lang.String cropImage, @org.jetbrains.annotations.Nullable()
    java.lang.String name, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.Integer> box, @org.jetbrains.annotations.Nullable()
    java.lang.String value, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.FaceData face, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.PlateData plate, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.AttributeData attribute, @org.jetbrains.annotations.Nullable()
    java.lang.String focusVideo, @org.jetbrains.annotations.Nullable()
    java.lang.Integer profileId, @org.jetbrains.annotations.Nullable()
    java.lang.String faceCrop, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.UniformData uniform, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.ObjectData objectData, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.WeaponData weapon, double similarity, @org.jetbrains.annotations.Nullable()
    java.lang.String containerId, @org.jetbrains.annotations.Nullable()
    java.lang.String cameraUUID, @org.jetbrains.annotations.Nullable()
    java.util.List<com.tenli.oneview.model.network.BoxData> boxes) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getType() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getImage() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getVideo() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCropImage() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.Integer> getBox() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getValue() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.FaceData getFace() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.PlateData getPlate() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.AttributeData getAttribute() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getFocusVideo() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getProfileId() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getFaceCrop() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.UniformData getUniform() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.ObjectData getObjectData() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.WeaponData getWeapon() {
        return null;
    }
    
    public final double getSimilarity() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getContainerId() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCameraUUID() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<com.tenli.oneview.model.network.BoxData> getBoxes() {
        return null;
    }
    
    public EventDataDetail() {
        super();
    }
}