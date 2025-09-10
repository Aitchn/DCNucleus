package io.aitchn.dcnucleus.api.console;

import java.util.Collections;
import java.util.List;

public interface ConsoleCommand {
    String getName();
    String getDescription();
    String getUsage();

    default List<String> getAliases() {
        return Collections.emptyList();
    }

    /**
     * 執行指令
     * @param args 指令參數
     * @return true = 成功, false = 失敗
     */
    boolean execute(String[] args);

    /**
     * Tab 補全 (可選)
     */
    default List<String> tabComplete(String[] args) {
        return Collections.emptyList();
    }
}
