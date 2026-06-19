package com.tenli.oneview.model.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0002\b\t\n\u0002\u0010\b\n\u0002\b)\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0087\b\u0018\u00002\u00020\u0001B\u00dd\u0001\u0012\u0010\b\u0002\u0010\u0002\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\u0010\b\u0002\u0010\u0007\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003\u0012\u0010\b\u0002\u0010\b\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003\u0012\u0010\b\u0002\u0010\t\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u000b\u0012\u0010\b\u0002\u0010\r\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000b\u0012\u0010\b\u0002\u0010\u000f\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u000b\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0004\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0004\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u0004\u0012\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u0015\u00a2\u0006\u0002\u0010\u0016J\u0011\u0010-\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0011\u0010.\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0010\u0010/\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0018J\u000b\u00100\u001a\u0004\u0018\u00010\u0004H\u00c6\u0003J\u000b\u00101\u001a\u0004\u0018\u00010\u0004H\u00c6\u0003J\u000b\u00102\u001a\u0004\u0018\u00010\u0004H\u00c6\u0003J\u0010\u00103\u001a\u0004\u0018\u00010\u0015H\u00c6\u0003\u00a2\u0006\u0002\u0010 J\u000b\u00104\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u0011\u00105\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0011\u00106\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0011\u00107\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0010\u00108\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0018J\u0010\u00109\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0018J\u0011\u0010:\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0010\u0010;\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0018J\u00e6\u0001\u0010<\u001a\u00020\u00002\u0010\b\u0002\u0010\u0002\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00062\u0010\b\u0002\u0010\u0007\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u00032\u0010\b\u0002\u0010\b\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u00032\u0010\b\u0002\u0010\t\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u00032\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u000b2\u0010\b\u0002\u0010\r\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u000b2\u0010\b\u0002\u0010\u000f\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u000b2\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u00042\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00042\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u00042\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u00c6\u0001\u00a2\u0006\u0002\u0010=J\u0013\u0010>\u001a\u00020?2\b\u0010@\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010A\u001a\u00020\u0015H\u00d6\u0001J\t\u0010B\u001a\u00020\u0004H\u00d6\u0001R\u0015\u0010\u0010\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\n\n\u0002\u0010\u0019\u001a\u0004\b\u0017\u0010\u0018R\u0019\u0010\u000f\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0019\u0010\r\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001bR\u0015\u0010\u000e\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\n\n\u0002\u0010\u0019\u001a\u0004\b\u001d\u0010\u0018R\u0019\u0010\t\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001bR\u0015\u0010\u0014\u001a\u0004\u0018\u00010\u0015\u00a2\u0006\n\n\u0002\u0010!\u001a\u0004\b\u001f\u0010 R\u0015\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\n\n\u0002\u0010\u0019\u001a\u0004\b\"\u0010\u0018R\u0019\u0010\b\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u001bR\u0019\u0010\u0002\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u001bR\u0015\u0010\f\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\n\n\u0002\u0010\u0019\u001a\u0004\b%\u0010\u0018R\u0019\u0010\u0007\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u001bR\u0013\u0010\u0013\u001a\u0004\u0018\u00010\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010(R\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010*R\u0013\u0010\u0011\u001a\u0004\u0018\u00010\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010(R\u0013\u0010\u0012\u001a\u0004\u0018\u00010\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010(\u00a8\u0006C"}, d2 = {"Lcom/tenli/oneview/model/network/SearchModel;", "", "objects", "", "", "time", "Lcom/tenli/oneview/model/network/SearchTimeRange;", "plates", "faces", "colors", "faceConfidence", "", "plateConfidence", "advancedText", "advancedTextConfidence", "advanceImages", "advanceImageConfidence", "timeSearch", "typeSearch", "searchMode", "count", "", "(Ljava/util/List;Lcom/tenli/oneview/model/network/SearchTimeRange;Ljava/util/List;Ljava/util/List;Ljava/util/List;Ljava/lang/Double;Ljava/lang/Double;Ljava/util/List;Ljava/lang/Double;Ljava/util/List;Ljava/lang/Double;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;)V", "getAdvanceImageConfidence", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getAdvanceImages", "()Ljava/util/List;", "getAdvancedText", "getAdvancedTextConfidence", "getColors", "getCount", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getFaceConfidence", "getFaces", "getObjects", "getPlateConfidence", "getPlates", "getSearchMode", "()Ljava/lang/String;", "getTime", "()Lcom/tenli/oneview/model/network/SearchTimeRange;", "getTimeSearch", "getTypeSearch", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/util/List;Lcom/tenli/oneview/model/network/SearchTimeRange;Ljava/util/List;Ljava/util/List;Ljava/util/List;Ljava/lang/Double;Ljava/lang/Double;Ljava/util/List;Ljava/lang/Double;Ljava/util/List;Ljava/lang/Double;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;)Lcom/tenli/oneview/model/network/SearchModel;", "equals", "", "other", "hashCode", "toString", "app_debug"})
public final class SearchModel {
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<java.lang.String> objects = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.SearchTimeRange time = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<java.lang.String> plates = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<java.lang.String> faces = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<java.lang.String> colors = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double faceConfidence = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double plateConfidence = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<java.lang.String> advancedText = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double advancedTextConfidence = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<java.lang.String> advanceImages = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double advanceImageConfidence = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String timeSearch = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String typeSearch = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String searchMode = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer count = null;
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> component10() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component12() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component13() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component14() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component15() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.SearchTimeRange component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.oneview.model.network.SearchModel copy(@org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> objects, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.SearchTimeRange time, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> plates, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> faces, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> colors, @org.jetbrains.annotations.Nullable()
    java.lang.Double faceConfidence, @org.jetbrains.annotations.Nullable()
    java.lang.Double plateConfidence, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> advancedText, @org.jetbrains.annotations.Nullable()
    java.lang.Double advancedTextConfidence, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> advanceImages, @org.jetbrains.annotations.Nullable()
    java.lang.Double advanceImageConfidence, @org.jetbrains.annotations.Nullable()
    java.lang.String timeSearch, @org.jetbrains.annotations.Nullable()
    java.lang.String typeSearch, @org.jetbrains.annotations.Nullable()
    java.lang.String searchMode, @org.jetbrains.annotations.Nullable()
    java.lang.Integer count) {
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
    
    public SearchModel(@org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> objects, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.SearchTimeRange time, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> plates, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> faces, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> colors, @org.jetbrains.annotations.Nullable()
    java.lang.Double faceConfidence, @org.jetbrains.annotations.Nullable()
    java.lang.Double plateConfidence, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> advancedText, @org.jetbrains.annotations.Nullable()
    java.lang.Double advancedTextConfidence, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> advanceImages, @org.jetbrains.annotations.Nullable()
    java.lang.Double advanceImageConfidence, @org.jetbrains.annotations.Nullable()
    java.lang.String timeSearch, @org.jetbrains.annotations.Nullable()
    java.lang.String typeSearch, @org.jetbrains.annotations.Nullable()
    java.lang.String searchMode, @org.jetbrains.annotations.Nullable()
    java.lang.Integer count) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> getObjects() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.SearchTimeRange getTime() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> getPlates() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> getFaces() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> getColors() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getFaceConfidence() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getPlateConfidence() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> getAdvancedText() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getAdvancedTextConfidence() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> getAdvanceImages() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getAdvanceImageConfidence() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getTimeSearch() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getTypeSearch() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSearchMode() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getCount() {
        return null;
    }
    
    public SearchModel() {
        super();
    }
}