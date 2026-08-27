import { useLingui } from '@lingui/react/macro'
import { useNavigation } from '@react-navigation/native'
import { PEARPASS_WEBSITE } from '@tetherto/pearpass-lib-constants'
import {
  Button,
  PageHeader,
  rawTokens,
  useTheme,
  Text
} from '@tetherto/pearpass-lib-ui-kit'
import { Send } from '@tetherto/pearpass-lib-ui-kit/icons'
import { Linking, StyleSheet, View } from 'react-native'
import { Layout } from 'src/containers/Layout'
import { BackScreenHeader } from 'src/containers/ScreenHeader/BackScreenHeader'

const FEEDBACK_URL = `${PEARPASS_WEBSITE}/contact/`

export const Feedback = () => {
  const { t } = useLingui()
  const navigation = useNavigation()
  const { theme } = useTheme()
  const colors = theme.colors

  return (
    <Layout
      scrollable
      header={
        <BackScreenHeader
          title={t`Settings`}
          onBack={() => navigation.goBack()}
        />
      }
      contentStyle={styles.content}
      footer={
        <Button
          variant="primary"
          fullWidth
          testID="feedback-open-button"
          onClick={() => Linking.openURL(FEEDBACK_URL)}
        >
          <View style={styles.sendButton}>
            <Send color={colors.colorOnPrimary} />
            <Text color={colors.colorOnPrimary}>{t`Open contact form`}</Text>
          </View>
        </Button>
      }
    >
      <PageHeader
        title={t`Report a problem`}
        subtitle={t`Opens the Lockwright contact form. Leave an email if you want a reply.`}
      />
    </Layout>
  )
}

const styles = StyleSheet.create({
  content: {
    padding: rawTokens.spacing16,
    paddingTop: rawTokens.spacing24,
    gap: rawTokens.spacing8,
    flexGrow: 1
  },
  sendButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: rawTokens.spacing8,
    display: 'flex'
  }
})
