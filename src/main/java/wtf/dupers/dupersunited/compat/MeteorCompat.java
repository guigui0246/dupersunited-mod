package wtf.dupers.dupersunited.compat;

import org.jetbrains.annotations.Nullable;
import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.features.ssidLogin.AccountsScreen;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class MeteorCompat {
    public static List<AccountsScreen.AccountEntry> getAccounts() {
        if (!FabricLoader.getInstance().isModLoaded("meteor-client")) {
            return List.of();
        }

        // dumb shit magic stuff

        try {
            Class<?> accountsClass = Class.forName("meteordevelopment.meteorclient.systems.accounts.Accounts");
            Method accounts_get_Method = accountsClass.getDeclaredMethod("get");
            Iterable<?> accounts = (Iterable<?>) accounts_get_Method.invoke(null);

            if (accounts == null) {
                return List.of();
            }

            Class<?> accountClass = Class.forName("meteordevelopment.meteorclient.systems.accounts.Account");
            Method account_fetchInfo_Method = accountClass.getDeclaredMethod("fetchInfo");
            Method account_getUsername_Method = accountClass.getDeclaredMethod("getUsername");

            @Nullable Class<?> sessionAccountClass;
            Field sessionAccount_accessToken_Field = null;
            try {
                sessionAccountClass = Class.forName("meteordevelopment.meteorclient.systems.accounts.types.SessionAccount");
                sessionAccount_accessToken_Field = sessionAccountClass.getDeclaredField("accessToken");
                sessionAccount_accessToken_Field.setAccessible(true);
            } catch (ClassNotFoundException e) {
                sessionAccountClass = null;
            }


            Class<?> microsoftAccountClass = Class.forName("meteordevelopment.meteorclient.systems.accounts.types.MicrosoftAccount");
            Field microsoftAccount_token_Field = microsoftAccountClass.getDeclaredField("token");
            microsoftAccount_token_Field.setAccessible(true);

            List<AccountsScreen.AccountEntry> meteorAccountEntries = new ArrayList<>();

            for (Object account : accounts) {
                String token;
                if (sessionAccountClass != null && sessionAccountClass.isInstance(account)) {
                    token = (String) sessionAccount_accessToken_Field.get(account);
                } else if (microsoftAccountClass.isInstance(account)) {
                    token = (String) microsoftAccount_token_Field.get(account);
                    if (token == null) {
                        boolean isValid = (boolean) account_fetchInfo_Method.invoke(account);
                        if (isValid) {
                            token = (String) microsoftAccount_token_Field.get(account);
                        } else {
                            continue;
                        }
                    }
                } else {
                    continue;
                }

                String username = (String) account_getUsername_Method.invoke(account);

                meteorAccountEntries.add(new AccountsScreen.AccountEntry(username, token, "Meteor"));
            }

            return meteorAccountEntries;
        } catch (ReflectiveOperationException e) {
            MainClient.LOGGER.error("Error loading meteor client compat", e);
            return List.of();
        }
    }
}
