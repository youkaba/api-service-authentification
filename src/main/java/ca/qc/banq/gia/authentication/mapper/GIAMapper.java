package ca.qc.banq.gia.authentication.mapper;

import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.models.AppPayload;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import static ca.qc.banq.gia.authentication.entities.AuthenticationType.B2C;

@Mapper(componentModel = "spring")
public interface GIAMapper {

    GIAMapper INSTANCE = Mappers.getMapper(GIAMapper.class);


    default AppPayload entityToAppPayload(App app, String loginUrl, String redirectApp, String loginOut) {
        return AppPayload.builder()
                .clientId(app.getClientId())
                .title(app.getTitle())
                .authenticationType(app.getAuthenticationType())
                .homeUrl(app.getHomeUrl())
                .certSecretValue(app.getCertSecretValue())
                .apiScope(app.getAuthenticationType().equals(B2C) ? app.getClientId() : "")
                .loginURL(loginUrl)
                .logoutURL(loginOut)
                .policySignUpSignIn(app.getPolicySignUpSignIn())
                .policyEditProfile(app.getPolicyEditProfile())
                .policyResetPassword(app.getPolicyResetPassword())
                .usersGroupId(app.getUsersGroupId())
                .redirectApp(redirectApp)
                .build();

    }

}
