package jsx.ink;

import org.apache.axis2.util.MetaDataEntry;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Test {
    public static void main(String[] args) throws Exception{
        InputStream resourceAsStream = Test.class.getResourceAsStream("/2.txt");
        DataInputStream dis=new DataInputStream(resourceAsStream);

        //MetaDataEntry利用链,71字节后,就是原生java反序列化payload
//        for(int a=0;a<71;a++){dis.read();
//        }
//        System.out.println(dis.readByte());//ac
//        System.out.println(dis.readByte());//ed
//        System.out.println(dis.readByte());//00
//        System.out.println(dis.readByte());//05

        //version,2字节
        int i = dis.readUnsignedShort();
        System.out.println("version:"+i);

        //headers,2字节
        int i1 = dis.readUnsignedShort();
        System.out.println("headers:"+i1);

        //bodyCount,2字节
        int i2 = dis.readUnsignedShort();
        System.out.println("bodyCount:"+i2);

        //targetUri,2字节
        String targetURI = dis.readUTF();
        System.out.println("targetURI:"+targetURI);

        //responseURI,2字节
        String responseURI = dis.readUTF();
        System.out.println("responseURI:"+responseURI);

        //一位int偏移,4字节
        int i3 = dis.readInt();
        System.out.println("偏移一位:"+i3);

        //amf0Input_type,1字节
        int amf0 = dis.readByte();
        System.out.println("amf0_type:"+amf0);

        //amf0Input_type,1字节
        int amf3 = dis.readByte();
        System.out.println("amf3_type:"+amf3);

        //readTraits,读取1byte,判断是否小于128,如果大于则继续读取,payload中该字节为7,直接返回
        // 决定externalizable和dynamic是否为true,和properties的count信息(属性长度)
        int i4 = readUInt29(dis);
        System.out.println("readTraits:"+i4);
        boolean externalizable = ((i4 & 4) == 4);
        boolean dynamic = ((i4 & 8) == 8);
        int count = (i4 >> 4); /* uint29 */

        //读取1byte,小于128则直接返回,该字节右移1位,除以2,表示类名长度.
        int len = readUInt29(dis);
        System.out.println("类名长度:"+(len>>1));

        //根据读取类名长度,读取类名,len>>1字节,MetaDataEntry利用链类名35字节
        byte[] bytearr = new byte[len>>1];
        dis.readFully(bytearr, 0, len>>1);
        System.out.println("类名:" + new String(bytearr));


        //**********************************************************
        System.out.println("以下是利用链数据,流中剩余数据交给利用链的readExternal方法处理");
        //**********************************************************


        //MetaDataEntry类序列化ID,8字节
        long suid = dis.readLong();
        System.out.println("序列化ID:"+suid);

        //revID=2,4字节
        int revID = dis.readInt();
        System.out.println("revID:"+revID);

        //isActive,1字节,必须为true,代码中会取反为false,否则直接return null;
        boolean isActive = dis.readBoolean();
        System.out.println("isActive:"+isActive);


        //isObjectForm,1字节,必须为false,
        //代码中不会走默认的readobject方法,
        //会将流中剩余数据再次封装为ObjectInputStream,并调用readObject方法
        boolean isObjectForm = dis.readBoolean();
        System.out.println("isObjectForm:"+isObjectForm);


        //java原生反序列化对象长度,4字节
        int size = dis.readInt();
        System.out.println("size:"+size);


        System.out.println(dis.readByte());//ac
        System.out.println(dis.readByte());//ed
        System.out.println(dis.readByte());//00
        System.out.println(dis.readByte());//05


        MetaDataEntry metaDataEntry = new MetaDataEntry();


    }


    public static  int readUInt29(DataInputStream in) throws IOException
    {
        int value;

        // Each byte must be treated as unsigned
        int b = in.readByte() & 0xFF;

        if (b < 128)
            return b;

        value = (b & 0x7F) << 7;
        b = in.readByte() & 0xFF;

        if (b < 128)
            return (value | b);

        value = (value | (b & 0x7F)) << 7;
        b = in.readByte() & 0xFF;

        if (b < 128)
            return (value | b);

        value = (value | (b & 0x7F)) << 8;
        b = in.readByte() & 0xFF;

        return (value | b);
    }
}
