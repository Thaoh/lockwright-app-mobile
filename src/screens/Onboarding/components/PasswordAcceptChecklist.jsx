import { StyleSheet, Text, View } from 'react-native'

import { PASSWORD_ACCEPT_RULE_KEYS } from '../../../utils/passwordPolicy'

const MARK_MET = '\u2713'
const MARK_UNMET = '\u25CB'

export const PasswordAcceptChecklist = ({
  ticks,
  labels,
  testID = 'password-accept-checklist'
}) => (
  <View testID={testID} style={styles.list} accessibilityRole="list">
    {PASSWORD_ACCEPT_RULE_KEYS.map((key) => {
      const met = Boolean(ticks?.[key])

      return (
        <View
          key={key}
          testID={`password-accept-rule-${key}`}
          accessibilityRole="checkbox"
          accessibilityState={{ checked: met }}
          style={styles.row}
        >
          <Text style={[styles.mark, met ? styles.markMet : styles.markUnmet]}>
            {met ? MARK_MET : MARK_UNMET}
          </Text>
          <Text
            style={[styles.label, met ? styles.labelMet : styles.labelUnmet]}
          >
            {labels[key]}
          </Text>
        </View>
      )
    })}
  </View>
)

const styles = StyleSheet.create({
  list: {
    gap: 6
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8
  },
  mark: {
    width: 16,
    fontSize: 14,
    lineHeight: 18
  },
  markMet: {
    color: '#A3E635'
  },
  markUnmet: {
    color: '#8B8B8B'
  },
  label: {
    fontSize: 13,
    lineHeight: 18
  },
  labelMet: {
    color: '#E8E8E8'
  },
  labelUnmet: {
    color: '#8B8B8B'
  }
})
