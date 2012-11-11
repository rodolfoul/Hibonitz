/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/rodolfo/workspace/HibonitApp/src/com/hibonit/app/LocationInterface.aidl
 */
package com.hibonit.app;
public interface LocationInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.hibonit.app.LocationInterface
{
private static final java.lang.String DESCRIPTOR = "com.hibonit.app.LocationInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.hibonit.app.LocationInterface interface,
 * generating a proxy if needed.
 */
public static com.hibonit.app.LocationInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.hibonit.app.LocationInterface))) {
return ((com.hibonit.app.LocationInterface)iin);
}
return new com.hibonit.app.LocationInterface.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getDistance:
{
data.enforceInterface(DESCRIPTOR);
double _result = this.getDistance();
reply.writeNoException();
reply.writeDouble(_result);
return true;
}
case TRANSACTION_getAverageSpeed:
{
data.enforceInterface(DESCRIPTOR);
double _result = this.getAverageSpeed();
reply.writeNoException();
reply.writeDouble(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.hibonit.app.LocationInterface
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public double getDistance() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
double _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getDistance, _data, _reply, 0);
_reply.readException();
_result = _reply.readDouble();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public double getAverageSpeed() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
double _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getAverageSpeed, _data, _reply, 0);
_reply.readException();
_result = _reply.readDouble();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getDistance = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getAverageSpeed = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public double getDistance() throws android.os.RemoteException;
public double getAverageSpeed() throws android.os.RemoteException;
}
