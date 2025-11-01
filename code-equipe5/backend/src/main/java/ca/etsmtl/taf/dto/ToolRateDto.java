package ca.etsmtl.taf.dto;

public class ToolRateDto {
    public String tool;
    public long passed;
    public long total;

    public ToolRateDto() {}
    public ToolRateDto(String tool, long passed, long total) {
        this.tool = tool; this.passed = passed; this.total = total;
    }
}