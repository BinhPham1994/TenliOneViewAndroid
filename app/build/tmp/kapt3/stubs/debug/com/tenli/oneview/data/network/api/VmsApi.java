package com.tenli.oneview.data.network.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00f4\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\f\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\b\u001a\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u001e\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u001e\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u001e\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u000fH\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u001e\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0012H\u00a7@\u00a2\u0006\u0002\u0010\u0013J\u001e\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0015H\u00a7@\u00a2\u0006\u0002\u0010\u0016J\u001e\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00180\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0018H\u00a7@\u00a2\u0006\u0002\u0010\u0019J\u001e\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001b0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u001bH\u00a7@\u00a2\u0006\u0002\u0010\u001cJ\u001e\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001e0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u001eH\u00a7@\u00a2\u0006\u0002\u0010\u001fJ\u001e\u0010 \u001a\b\u0012\u0004\u0012\u00020!0\u00032\b\b\u0001\u0010\u0005\u001a\u00020!H\u00a7@\u00a2\u0006\u0002\u0010\"J\u001e\u0010#\u001a\b\u0012\u0004\u0012\u00020$0\u00032\b\b\u0001\u0010\u0005\u001a\u00020$H\u00a7@\u00a2\u0006\u0002\u0010%J\u001e\u0010&\u001a\b\u0012\u0004\u0012\u00020\'0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\'H\u00a7@\u00a2\u0006\u0002\u0010(J\u001e\u0010)\u001a\b\u0012\u0004\u0012\u00020*0\u00032\b\b\u0001\u0010\u0005\u001a\u00020*H\u00a7@\u00a2\u0006\u0002\u0010+J\u001e\u0010,\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010-\u001a\u00020.H\u00a7@\u00a2\u0006\u0002\u0010/J\u001e\u00100\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010-\u001a\u00020.H\u00a7@\u00a2\u0006\u0002\u0010/J\u001e\u00101\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010-\u001a\u00020.H\u00a7@\u00a2\u0006\u0002\u0010/J\u001e\u00102\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010-\u001a\u00020.H\u00a7@\u00a2\u0006\u0002\u0010/J\u001e\u00103\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010-\u001a\u00020.H\u00a7@\u00a2\u0006\u0002\u0010/J\u001e\u00104\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010-\u001a\u00020.H\u00a7@\u00a2\u0006\u0002\u0010/J\u001e\u00105\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010-\u001a\u00020.H\u00a7@\u00a2\u0006\u0002\u0010/J\u001e\u00106\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010-\u001a\u00020.H\u00a7@\u00a2\u0006\u0002\u0010/J\u001e\u00107\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010-\u001a\u00020.H\u00a7@\u00a2\u0006\u0002\u0010/J\u001e\u00108\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010-\u001a\u00020.H\u00a7@\u00a2\u0006\u0002\u0010/J\u001e\u00109\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010-\u001a\u00020.H\u00a7@\u00a2\u0006\u0002\u0010/J\u001e\u0010:\u001a\b\u0012\u0004\u0012\u00020;0\u00032\b\b\u0001\u0010<\u001a\u00020=H\u00a7@\u00a2\u0006\u0002\u0010>J(\u0010?\u001a\b\u0012\u0004\u0012\u00020;0\u00032\b\b\u0001\u0010@\u001a\u00020A2\b\b\u0001\u0010<\u001a\u00020=H\u00a7@\u00a2\u0006\u0002\u0010BJ\u001a\u0010C\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0D0\u0003H\u00a7@\u00a2\u0006\u0002\u0010EJ\u001a\u0010F\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0D0\u0003H\u00a7@\u00a2\u0006\u0002\u0010EJ\u001e\u0010G\u001a\b\u0012\u0004\u0012\u00020H0\u00032\b\b\u0001\u0010I\u001a\u00020.H\u00a7@\u00a2\u0006\u0002\u0010/J\u001a\u0010J\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120D0\u0003H\u00a7@\u00a2\u0006\u0002\u0010EJ\u001a\u0010K\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0D0\u0003H\u00a7@\u00a2\u0006\u0002\u0010EJ\u001a\u0010L\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150D0\u0003H\u00a7@\u00a2\u0006\u0002\u0010EJ\u001a\u0010M\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001b0D0\u0003H\u00a7@\u00a2\u0006\u0002\u0010EJ\u001e\u0010N\u001a\b\u0012\u0004\u0012\u00020O0\u00032\b\b\u0001\u0010P\u001a\u00020.H\u00a7@\u00a2\u0006\u0002\u0010/J\u001a\u0010Q\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001e0D0\u0003H\u00a7@\u00a2\u0006\u0002\u0010EJ\u001e\u0010R\u001a\b\u0012\u0004\u0012\u00020S0\u00032\b\b\u0001\u0010@\u001a\u00020AH\u00a7@\u00a2\u0006\u0002\u0010TJ\u0014\u0010U\u001a\b\u0012\u0004\u0012\u00020V0\u0003H\u00a7@\u00a2\u0006\u0002\u0010EJ\u001e\u0010W\u001a\b\u0012\u0004\u0012\u00020!0\u00032\b\b\u0001\u0010-\u001a\u00020.H\u00a7@\u00a2\u0006\u0002\u0010/J\u001e\u0010X\u001a\b\u0012\u0004\u0012\u00020!0\u00032\b\b\u0001\u0010@\u001a\u00020AH\u00a7@\u00a2\u0006\u0002\u0010TJ\u001a\u0010Y\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020$0D0\u0003H\u00a7@\u00a2\u0006\u0002\u0010EJ\u001a\u0010Z\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\'0D0\u0003H\u00a7@\u00a2\u0006\u0002\u0010EJ\u001a\u0010[\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020!0D0\u0003H\u00a7@\u00a2\u0006\u0002\u0010EJ\u001a\u0010\\\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020*0D0\u0003H\u00a7@\u00a2\u0006\u0002\u0010EJ\u001e\u0010]\u001a\b\u0012\u0004\u0012\u00020^0\u00032\b\b\u0001\u0010I\u001a\u00020.H\u00a7@\u00a2\u0006\u0002\u0010/JJ\u0010_\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020`0D0\u00032\n\b\u0003\u0010a\u001a\u0004\u0018\u00010.2\n\b\u0003\u0010b\u001a\u0004\u0018\u00010.2\n\b\u0003\u0010c\u001a\u0004\u0018\u00010A2\n\b\u0003\u0010d\u001a\u0004\u0018\u00010AH\u00a7@\u00a2\u0006\u0002\u0010eJ\u001e\u0010f\u001a\b\u0012\u0004\u0012\u00020g0\u00032\b\b\u0001\u0010\u0005\u001a\u00020hH\u00a7@\u00a2\u0006\u0002\u0010iJ(\u0010j\u001a\b\u0012\u0004\u0012\u00020g0\u00032\b\b\u0001\u0010@\u001a\u00020A2\b\b\u0001\u0010\u0005\u001a\u00020hH\u00a7@\u00a2\u0006\u0002\u0010kJ\u001e\u0010l\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020mH\u00a7@\u00a2\u0006\u0002\u0010nJ\u001e\u0010o\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020pH\u00a7@\u00a2\u0006\u0002\u0010qJ*\u0010r\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0014\b\u0001\u0010s\u001a\u000e\u0012\u0004\u0012\u00020A\u0012\u0004\u0012\u00020A0tH\u00a7@\u00a2\u0006\u0002\u0010uJ4\u0010v\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010@\u001a\u00020A2\u0014\b\u0001\u0010s\u001a\u000e\u0012\u0004\u0012\u00020A\u0012\u0004\u0012\u00020A0tH\u00a7@\u00a2\u0006\u0002\u0010wJ\u001e\u0010x\u001a\b\u0012\u0004\u0012\u00020V0\u00032\b\b\u0001\u0010\u0005\u001a\u00020VH\u00a7@\u00a2\u0006\u0002\u0010yJ(\u0010z\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010-\u001a\u00020.2\b\b\u0001\u0010\u0005\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010{J(\u0010|\u001a\b\u0012\u0004\u0012\u00020\f0\u00032\b\b\u0001\u0010-\u001a\u00020.2\b\b\u0001\u0010\u0005\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010}J(\u0010~\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00032\b\b\u0001\u0010-\u001a\u00020.2\b\b\u0001\u0010\u0005\u001a\u00020\u000fH\u00a7@\u00a2\u0006\u0002\u0010\u007fJ*\u0010\u0080\u0001\u001a\b\u0012\u0004\u0012\u00020\u00120\u00032\b\b\u0001\u0010-\u001a\u00020.2\b\b\u0001\u0010\u0005\u001a\u00020\u0012H\u00a7@\u00a2\u0006\u0003\u0010\u0081\u0001J*\u0010\u0082\u0001\u001a\b\u0012\u0004\u0012\u00020\u00150\u00032\b\b\u0001\u0010-\u001a\u00020.2\b\b\u0001\u0010\u0005\u001a\u00020\u0015H\u00a7@\u00a2\u0006\u0003\u0010\u0083\u0001J*\u0010\u0084\u0001\u001a\b\u0012\u0004\u0012\u00020\u001e0\u00032\b\b\u0001\u0010-\u001a\u00020.2\b\b\u0001\u0010\u0005\u001a\u00020\u001eH\u00a7@\u00a2\u0006\u0003\u0010\u0085\u0001J*\u0010\u0086\u0001\u001a\b\u0012\u0004\u0012\u00020!0\u00032\b\b\u0001\u0010-\u001a\u00020.2\b\b\u0001\u0010\u0005\u001a\u00020!H\u00a7@\u00a2\u0006\u0003\u0010\u0087\u0001J*\u0010\u0088\u0001\u001a\b\u0012\u0004\u0012\u00020$0\u00032\b\b\u0001\u0010-\u001a\u00020.2\b\b\u0001\u0010\u0005\u001a\u00020$H\u00a7@\u00a2\u0006\u0003\u0010\u0089\u0001J*\u0010\u008a\u0001\u001a\b\u0012\u0004\u0012\u00020\'0\u00032\b\b\u0001\u0010-\u001a\u00020.2\b\b\u0001\u0010\u0005\u001a\u00020\'H\u00a7@\u00a2\u0006\u0003\u0010\u008b\u0001J*\u0010\u008c\u0001\u001a\b\u0012\u0004\u0012\u00020*0\u00032\b\b\u0001\u0010-\u001a\u00020.2\b\b\u0001\u0010\u0005\u001a\u00020*H\u00a7@\u00a2\u0006\u0003\u0010\u008d\u0001\u00a8\u0006\u008e\u0001"}, d2 = {"Lcom/tenli/oneview/data/network/api/VmsApi;", "", "changePassword", "Lretrofit2/Response;", "", "model", "Lcom/tenli/oneview/model/network/ChangePasswordModel;", "(Lcom/tenli/oneview/model/network/ChangePasswordModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createCamera", "Lcom/tenli/oneview/model/network/CameraModel;", "(Lcom/tenli/oneview/model/network/CameraModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createCameraGroup", "Lcom/tenli/oneview/model/network/CameraGroupModel;", "(Lcom/tenli/oneview/model/network/CameraGroupModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createCameraGroupAccess", "Lcom/tenli/oneview/model/network/CameraGroupAccessModel;", "(Lcom/tenli/oneview/model/network/CameraGroupAccessModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createCameraInGroup", "Lcom/tenli/oneview/model/network/CameraInGroupModel;", "(Lcom/tenli/oneview/model/network/CameraInGroupModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createCameraView", "Lcom/tenli/oneview/model/network/CameraViewModel;", "(Lcom/tenli/oneview/model/network/CameraViewModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createLiveStream", "Lcom/tenli/oneview/model/network/LiveStreamModel;", "(Lcom/tenli/oneview/model/network/LiveStreamModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createPermanentLink", "Lcom/tenli/oneview/model/network/PermanentLinkModel;", "(Lcom/tenli/oneview/model/network/PermanentLinkModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createStorage", "Lcom/tenli/oneview/model/network/StorageModel;", "(Lcom/tenli/oneview/model/network/StorageModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createUser", "Lcom/tenli/oneview/model/network/UserModel;", "(Lcom/tenli/oneview/model/network/UserModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createUserGroup", "Lcom/tenli/oneview/model/network/UserGroupModel;", "(Lcom/tenli/oneview/model/network/UserGroupModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createUserInGroup", "Lcom/tenli/oneview/model/network/UserInGroupModel;", "(Lcom/tenli/oneview/model/network/UserInGroupModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createVMSService", "Lcom/tenli/oneview/model/network/VMSServiceModel;", "(Lcom/tenli/oneview/model/network/VMSServiceModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteCamera", "id", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteCameraGroup", "deleteCameraGroupAccess", "deleteCameraInGroup", "deleteCameraView", "deletePermanentLink", "deleteStorage", "deleteUser", "deleteUserGroup", "deleteUserInGroup", "deleteVMSService", "getAuthToken", "Lcom/tenli/oneview/model/network/AuthTokenModel;", "request", "Lcom/tenli/oneview/model/network/AuthTokenRequest;", "(Lcom/tenli/oneview/model/network/AuthTokenRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAuthTokenExt", "url", "", "(Ljava/lang/String;Lcom/tenli/oneview/model/network/AuthTokenRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCameraGroupAccessList", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCameraGroupList", "getCameraImageLink", "Lcom/tenli/oneview/model/network/CameraImageLinkModel;", "cameraId", "getCameraInGroupList", "getCameraList", "getCameraViewList", "getPermanentLinkList", "getReportLink", "Lcom/tenli/oneview/model/network/ReportLinkModel;", "vmsId", "getStorageList", "getStorageStatus", "Lcom/tenli/oneview/model/network/StorageStatusModel;", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getSystemSetting", "Lcom/tenli/oneview/model/network/SystemSettingModel;", "getUser", "getUserExt", "getUserGroupList", "getUserInGroupList", "getUserList", "getVMSServiceList", "getVideoJoinLink", "Lcom/tenli/oneview/model/network/VideoLinkModel;", "getVideoList", "Lcom/tenli/oneview/model/network/VideoModel;", "camera", "count", "from", "to", "(Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "logIn", "Lcom/tenli/oneview/model/network/TokenModel;", "Lcom/tenli/oneview/model/network/LogInModel;", "(Lcom/tenli/oneview/model/network/LogInModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "logInExt", "(Ljava/lang/String;Lcom/tenli/oneview/model/network/LogInModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "logOut", "Lcom/tenli/oneview/model/network/LogOutModel;", "(Lcom/tenli/oneview/model/network/LogOutModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "resetPassword", "Lcom/tenli/oneview/model/network/ResetPasswordModel;", "(Lcom/tenli/oneview/model/network/ResetPasswordModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "restartService", "command", "", "(Ljava/util/Map;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "restartServiceExt", "(Ljava/lang/String;Ljava/util/Map;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveSystemSetting", "(Lcom/tenli/oneview/model/network/SystemSettingModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateCamera", "(ILcom/tenli/oneview/model/network/CameraModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateCameraGroup", "(ILcom/tenli/oneview/model/network/CameraGroupModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateCameraGroupAccess", "(ILcom/tenli/oneview/model/network/CameraGroupAccessModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateCameraInGroup", "(ILcom/tenli/oneview/model/network/CameraInGroupModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateCameraView", "(ILcom/tenli/oneview/model/network/CameraViewModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateStorage", "(ILcom/tenli/oneview/model/network/StorageModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateUser", "(ILcom/tenli/oneview/model/network/UserModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateUserGroup", "(ILcom/tenli/oneview/model/network/UserGroupModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateUserInGroup", "(ILcom/tenli/oneview/model/network/UserInGroupModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateVMSService", "(ILcom/tenli/oneview/model/network/VMSServiceModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface VmsApi {
    
    @retrofit2.http.GET(value = "VMS/api/VMS")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getVMSServiceList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.VMSServiceModel>>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/VMS")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createVMSService(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.VMSServiceModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.VMSServiceModel>> $completion);
    
    @retrofit2.http.PUT(value = "VMS/api/VMS/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateVMSService(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.VMSServiceModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.VMSServiceModel>> $completion);
    
    @retrofit2.http.DELETE(value = "VMS/api/VMS/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteVMSService(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "VMS/api/CameraView")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCameraViewList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.CameraViewModel>>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/CameraView")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createCameraView(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.CameraViewModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.CameraViewModel>> $completion);
    
    @retrofit2.http.PUT(value = "VMS/api/CameraView/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateCameraView(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.CameraViewModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.CameraViewModel>> $completion);
    
    @retrofit2.http.DELETE(value = "VMS/api/CameraView/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteCameraView(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "VMS/api/UserGroup")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUserGroupList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.UserGroupModel>>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/UserGroup")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createUserGroup(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.UserGroupModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.UserGroupModel>> $completion);
    
    @retrofit2.http.PUT(value = "VMS/api/UserGroup/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateUserGroup(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.UserGroupModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.UserGroupModel>> $completion);
    
    @retrofit2.http.DELETE(value = "VMS/api/UserGroup/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteUserGroup(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "VMS/api/User")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUserList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.UserModel>>> $completion);
    
    @retrofit2.http.GET(value = "VMS/api/User/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUser(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.UserModel>> $completion);
    
    @retrofit2.http.GET()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUserExt(@retrofit2.http.Url()
    @org.jetbrains.annotations.NotNull()
    java.lang.String url, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.UserModel>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/User")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createUser(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.UserModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.UserModel>> $completion);
    
    @retrofit2.http.PUT(value = "VMS/api/User/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateUser(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.UserModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.UserModel>> $completion);
    
    @retrofit2.http.DELETE(value = "VMS/api/User/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteUser(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "VMS/api/UserInGroup")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUserInGroupList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.UserInGroupModel>>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/UserInGroup")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createUserInGroup(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.UserInGroupModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.UserInGroupModel>> $completion);
    
    @retrofit2.http.PUT(value = "VMS/api/UserInGroup/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateUserInGroup(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.UserInGroupModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.UserInGroupModel>> $completion);
    
    @retrofit2.http.DELETE(value = "VMS/api/UserInGroup/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteUserInGroup(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "VMS/api/Storage")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getStorageList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.StorageModel>>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/Storage")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createStorage(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.StorageModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.StorageModel>> $completion);
    
    @retrofit2.http.PUT(value = "VMS/api/Storage/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateStorage(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.StorageModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.StorageModel>> $completion);
    
    @retrofit2.http.DELETE(value = "VMS/api/Storage/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteStorage(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/AuthToken")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAuthToken(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.AuthTokenRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.AuthTokenModel>> $completion);
    
    /**
     * AuthToken với URL tùy chỉnh (tương đương postExt trên web)
     */
    @retrofit2.http.POST()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAuthTokenExt(@retrofit2.http.Url()
    @org.jetbrains.annotations.NotNull()
    java.lang.String url, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.AuthTokenRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.AuthTokenModel>> $completion);
    
    @retrofit2.http.GET(value = "VMS/api/CameraGroup")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCameraGroupList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.CameraGroupModel>>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/CameraGroup")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createCameraGroup(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.CameraGroupModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.CameraGroupModel>> $completion);
    
    @retrofit2.http.PUT(value = "VMS/api/CameraGroup/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateCameraGroup(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.CameraGroupModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.CameraGroupModel>> $completion);
    
    @retrofit2.http.DELETE(value = "VMS/api/CameraGroup/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteCameraGroup(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "VMS/api/Camera")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCameraList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.CameraModel>>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/Camera")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createCamera(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.CameraModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.CameraModel>> $completion);
    
    @retrofit2.http.PUT(value = "VMS/api/Camera/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateCamera(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.CameraModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.CameraModel>> $completion);
    
    @retrofit2.http.DELETE(value = "VMS/api/Camera/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteCamera(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "VMS/api/CameraInGroup")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCameraInGroupList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.CameraInGroupModel>>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/CameraInGroup")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createCameraInGroup(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.CameraInGroupModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.CameraInGroupModel>> $completion);
    
    @retrofit2.http.PUT(value = "VMS/api/CameraInGroup/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateCameraInGroup(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.CameraInGroupModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.CameraInGroupModel>> $completion);
    
    @retrofit2.http.DELETE(value = "VMS/api/CameraInGroup/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteCameraInGroup(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/LiveStream")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createLiveStream(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.LiveStreamModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.LiveStreamModel>> $completion);
    
    @retrofit2.http.GET(value = "VMS/api/VideoList")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getVideoList(@retrofit2.http.Query(value = "camera")
    @org.jetbrains.annotations.Nullable()
    java.lang.Integer camera, @retrofit2.http.Query(value = "count")
    @org.jetbrains.annotations.Nullable()
    java.lang.Integer count, @retrofit2.http.Query(value = "from")
    @org.jetbrains.annotations.Nullable()
    java.lang.String from, @retrofit2.http.Query(value = "to")
    @org.jetbrains.annotations.Nullable()
    java.lang.String to, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.VideoModel>>> $completion);
    
    @retrofit2.http.GET(value = "VMS/api/PermanentLink")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getPermanentLinkList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.PermanentLinkModel>>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/PermanentLink")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createPermanentLink(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.PermanentLinkModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.PermanentLinkModel>> $completion);
    
    @retrofit2.http.DELETE(value = "VMS/api/PermanentLink/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deletePermanentLink(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "VMS/api/CameraGroupAccess")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCameraGroupAccessList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.CameraGroupAccessModel>>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/CameraGroupAccess")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createCameraGroupAccess(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.CameraGroupAccessModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.CameraGroupAccessModel>> $completion);
    
    @retrofit2.http.PUT(value = "VMS/api/CameraGroupAccess/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateCameraGroupAccess(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.CameraGroupAccessModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.CameraGroupAccessModel>> $completion);
    
    @retrofit2.http.DELETE(value = "VMS/api/CameraGroupAccess/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteCameraGroupAccess(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "VMS/api/ReportLink/{vmsId}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getReportLink(@retrofit2.http.Path(value = "vmsId")
    int vmsId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.ReportLinkModel>> $completion);
    
    @retrofit2.http.GET(value = "VMS/api/VideoJoinLink/{cameraId}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getVideoJoinLink(@retrofit2.http.Path(value = "cameraId")
    int cameraId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.VideoLinkModel>> $completion);
    
    @retrofit2.http.GET(value = "VMS/api/CameraImageLink/{cameraId}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCameraImageLink(@retrofit2.http.Path(value = "cameraId")
    int cameraId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.CameraImageLinkModel>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/SystemSetting")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object saveSystemSetting(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.SystemSettingModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.SystemSettingModel>> $completion);
    
    @retrofit2.http.GET(value = "VMS/api/SystemSetting")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getSystemSetting(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.SystemSettingModel>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/SystemControl")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object restartService(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> command, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    /**
     * Restart service với URL tùy chỉnh (tương đương restartServiceExt trên web)
     */
    @retrofit2.http.POST()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object restartServiceExt(@retrofit2.http.Url()
    @org.jetbrains.annotations.NotNull()
    java.lang.String url, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> command, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    /**
     * StorageStatus dùng host riêng của VMS service (tương đương getExt trên web)
     */
    @retrofit2.http.GET()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getStorageStatus(@retrofit2.http.Url()
    @org.jetbrains.annotations.NotNull()
    java.lang.String url, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.StorageStatusModel>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/LogIn")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object logIn(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.LogInModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.TokenModel>> $completion);
    
    @retrofit2.http.POST()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object logInExt(@retrofit2.http.Url()
    @org.jetbrains.annotations.NotNull()
    java.lang.String url, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.LogInModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.TokenModel>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/LogOut")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object logOut(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.LogOutModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/ChangePassword")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object changePassword(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.ChangePasswordModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.POST(value = "VMS/api/ResetPassword")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object resetPassword(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.ResetPasswordModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}