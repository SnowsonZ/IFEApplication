package snowson.ife.com.ifeapplication.bean;



public class Ads {
    
    public static final int ADS_MEDIA_PIC_TYPE = 1; // picture
    public static final int ADS_MEDIA_VIDEO_TYPE = 2; // video
    public static final int ADS_MEDIA_GIF_TYPE = 3; // gif

    private String id;

    private Byte mediaType;

    private Byte type;

    private String path;

    private int relateId;

    private String relateData;

    private String relateType;

    private String focus;

    public String getRelateType() {
        return relateType;
    }

    public void setRelateType(String RelateType) {
        this.relateType = RelateType == null ? null : RelateType.trim();
    }

    public String getRelateData() {
        return relateData;
    }

    public void setRelateData(String RelateData) {
        this.relateData = RelateData == null ? null : RelateData.trim();
    }

    public String getId() {
        return id;
    }

    public void setId(String Id) {
        this.id = Id == null ? null : Id.trim();
    }

    public Byte getMediaType() {
        return mediaType;
    }

    public void setMediaType(Byte MediaType) {
        this.mediaType = MediaType;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte Type) {
        this.type = Type;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String Path) {
        this.path = Path == null ? null : Path.trim();
    }

    public int getRelateId() {
        return relateId;
    }

    public void setRelateId(int RelateId) {
        this.relateId = RelateId;
    }

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }
}
