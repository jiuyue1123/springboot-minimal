package com.example.common.result;

import com.example.common.exception.ErrorCode;
import lombok.Getter;

import java.io.Serial;
import java.util.List;

/**
 * Paginated API response envelope.
 *
 * <p>Shares {@code code} and {@code message} with {@link Result}, and adds pagination
 * metadata.</p>
 *
 * @param <T> element type
 * @author nanak
 */
@Getter
public class PageResult<T> extends Result<List<T>> {

    @Serial
    private static final long serialVersionUID = 1L;

    private final long total;
    private final long pageNum;
    private final long pageSize;

    private PageResult(ErrorCode errorCode, List<T> data, long total, long pageNum, long pageSize) {
        super(errorCode, data);
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public static <T> PageResult<T> success(List<T> data, long total, long pageNum, long pageSize) {
        return new PageResult<>(ErrorCode.SUCCESS, data, total, pageNum, pageSize);
    }
}
